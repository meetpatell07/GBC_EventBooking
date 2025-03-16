package ca.gbc.eventservice.service;

import ca.gbc.eventservice.client.BookingClient;
import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.event.BookingEvent;
import ca.gbc.eventservice.model.Event;
import ca.gbc.eventservice.repository.EventRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final BookingClient bookingClient;

    private static final int STUDENT_LIMIT = 50;
    private static final int STAFF_LIMIT = 100;
    private static final int FACULTY_LIMIT = 200;

    @Setter
    @Getter
    private String eventType;

    @Setter
    @Getter
    private Integer expectedAttendees;


    @KafkaListener(topics = "booking", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeBookingEvent(BookingEvent bookingEvent) {
        log.info("Received BookingEvent from Kafka: {}", bookingEvent);
        try {
            registerEvent(bookingEvent);
        } catch (Exception e) {
            log.error("Error processing BookingEvent: {}", bookingEvent, e);
        }
    }

    private void registerEvent(BookingEvent bookingEvent) {
        try {
            Event event = Event.builder()
                    .eventName(bookingEvent.getPurpose())
                    .organizerId(bookingEvent.getUserId())
                    .roomId(bookingEvent.getRoomId())
                    .eventType(eventType)
                    .expectedAttendees(expectedAttendees)
                    .startTime(bookingEvent.getStartTime())
                    .endTime(bookingEvent.getEndTime())
                    .bookingId(bookingEvent.getBookingId())
                    .status("PENDING")
                    .build();

            eventRepository.save(event);
            log.info("Registered Event for Booking ID: {}", bookingEvent.getBookingId());
        } catch (Exception e) {
            log.error("Error registering event for Booking ID: {}", bookingEvent.getBookingId(), e);
        }
    }


    @Override
    public EventResponse createEvent(EventRequest eventRequest, String status, String bookingId) {
        Event event = Event.builder()
                .eventName(eventRequest.eventName())
                .organizerId(eventRequest.organizerId())
                .eventType(eventRequest.eventType())
                .expectedAttendees(eventRequest.expectedAttendees())
                .startTime(eventRequest.startTime())
                .endTime(eventRequest.endTime())
                .roomId(eventRequest.roomId())
                .bookingId(bookingId)
                .status(status)
                .build();

        eventRepository.save(event);
        return mapToEventResponse(event);
    }

    @Override
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll()
                .stream()
                .map(this::mapToEventResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EventResponse getEventById(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        return mapToEventResponse(event);
    }

    @Override
    public EventResponse updateEvent(String eventId, EventRequest eventRequest, String status, String bookingId) {
        Event existingEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        boolean isUpdated = false;

        if (!existingEvent.getEventName().equals(eventRequest.eventName())) {
            existingEvent.setEventName(eventRequest.eventName());
            isUpdated = true;
        }

        if (!existingEvent.getEventType().equals(eventRequest.eventType())) {
            existingEvent.setEventType(eventRequest.eventType());
            isUpdated = true;
        }

        if (existingEvent.getExpectedAttendees() != eventRequest.expectedAttendees()) {
            existingEvent.setExpectedAttendees(eventRequest.expectedAttendees());
            isUpdated = true;
        }

        if (!existingEvent.getStartTime().equals(eventRequest.startTime())) {
            existingEvent.setStartTime(eventRequest.startTime());
            isUpdated = true;
        }

        if (!existingEvent.getEndTime().equals(eventRequest.endTime())) {
            existingEvent.setEndTime(eventRequest.endTime());
            isUpdated = true;
        }

        if (!existingEvent.getRoomId().equals(eventRequest.roomId())) {
            existingEvent.setRoomId(eventRequest.roomId());
            isUpdated = true;
        }

        if (!existingEvent.getBookingId().equals(bookingId)) {
            existingEvent.setBookingId(bookingId);
            isUpdated = true;
        }

        if (!existingEvent.getStatus().equals(status)) {
            existingEvent.setStatus(status);
            isUpdated = true;
        }

        if (isUpdated) {
            eventRepository.save(existingEvent);
        }

        return mapToEventResponse(existingEvent);
    }



    @Override
    public void deleteEvent(String eventId) {
        log.info("Attempting to delete event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        try {
            log.info("Deleting booking with ID: {}", event.getBookingId());
            log.info("Booking deleted successfully.");
        } catch (Exception e) {
            log.error("Failed to delete booking for event ID: {} - {}", eventId, e.getMessage());
            throw new RuntimeException("Failed to delete associated booking for event ID: " + eventId, e);
        }

        log.info("Deleting event with ID: {}", eventId);
        bookingClient.deleteBooking(event.getBookingId());
        eventRepository.deleteById(eventId);
        log.info("Event deleted successfully with ID: {}", eventId);
    }


    @Override
    public boolean isAllowedToCreateEvent(String userType, int expectedAttendees) {
        return !switch (userType.toUpperCase()) {
            case "STUDENT" -> expectedAttendees <= STUDENT_LIMIT;
            case "STAFF" -> expectedAttendees <= STAFF_LIMIT;
            case "FACULTY" -> expectedAttendees <= FACULTY_LIMIT;
            default -> false;
        };
    }

    @Override
    public void updateStatus(String eventId, String status) {
        log.info("Attempting to update status for event ID: {}", eventId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));
        event.setStatus(status);
        eventRepository.save(event);
        log.info("Status updated to {} for event ID: {}", status, eventId);
    }

    private EventResponse mapToEventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getEventName(),
                event.getOrganizerId(),
                event.getEventType(),
                event.getExpectedAttendees(),
                event.getStartTime(),
                event.getEndTime(),
                event.getRoomId(),
                event.getBookingId(),
                event.getStatus()
        );
    }
}

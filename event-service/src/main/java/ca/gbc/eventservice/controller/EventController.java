package ca.gbc.eventservice.controller;
import ca.gbc.eventservice.client.BookingClient;
import ca.gbc.eventservice.client.RoomClient;
import ca.gbc.eventservice.client.UserClient;
import ca.gbc.eventservice.dto.BookingRequest;
import ca.gbc.eventservice.dto.BookingResponse;
import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import ca.gbc.eventservice.service.EventService;
import ca.gbc.eventservice.service.EventServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final UserClient userClient;
    private final RoomClient roomClient;
    private final BookingClient bookingClient;
    private final EventServiceImpl eventServiceImpl;


//    @PostMapping
//    public ResponseEntity<?> createEvent(@RequestBody EventRequest eventRequest) {
//        try {
//            String userType = getUserTypeOrHandleError(eventRequest.organizerId());
//            if (userType == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body("The specified organizer does not exist.");
//            }
//            if (eventService.isAllowedToCreateEvent(userType, eventRequest.expectedAttendees())) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                        .body("You have exceeded the allowed number of attendees for your role.");
//            }
//            if (!roomClient.checkRoomExists(eventRequest.roomId())) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body("The specified room does not exist.");
//            }
//            if (!roomClient.isCapacitySufficient(eventRequest.roomId(), eventRequest.expectedAttendees())) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body("The selected room does not have enough capacity for the expected number of attendees.");
//            }
//            ResponseEntity<Boolean> roomAvailabilityResponse = bookingClient.isRoomAvailable(
//                    eventRequest.roomId(),
//                    eventRequest.startTime().toString(),
//                    eventRequest.endTime().toString()
//            );
//
//            if (!roomAvailabilityResponse.getStatusCode().is2xxSuccessful() || Boolean.FALSE.equals(roomAvailabilityResponse.getBody())) {
//                return ResponseEntity.status(HttpStatus.CONFLICT)
//                        .body("The room is already booked for the requested time.");
//            }
//
//            BookingRequest bookingRequest = new BookingRequest(
//                    eventRequest.organizerId(),
//                    eventRequest.roomId(),
//                    eventRequest.startTime(),
//                    eventRequest.endTime(),
//                    eventRequest.eventName()
//            );
//            ResponseEntity<BookingResponse> bookingResponseEntity = bookingClient.createEventBooking(bookingRequest);
//            if (!bookingResponseEntity.getStatusCode().is2xxSuccessful() || bookingResponseEntity.getBody() == null) {
//                String failureMessage = bookingResponseEntity.getBody() != null
//                        ? bookingResponseEntity.getBody().purpose()
//                        : "Booking creation failed.";
//                return ResponseEntity.status(bookingResponseEntity.getStatusCode())
//                        .body(failureMessage);
//            }
//
//            BookingResponse bookingResponse = bookingResponseEntity.getBody();
//            String status = switch (userType.toUpperCase()) {
//                case "FACULTY", "STUDENT", "STAFF" -> "PENDING";
//                default -> "UNKNOWN";
//            };
//            EventResponse eventResponse = eventService.createEvent(eventRequest, status, bookingResponse.id());
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("Location", "/api/events/" + eventResponse.id());
//            return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(eventResponse);
//
//        } catch (Exception e) {
//            log.error("An error occurred while creating the event: {}", e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body("An error occurred while creating the event. Please check your request and try again.");
//        }
//    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventRequest eventRequest) {
        try {
            // Validate user type
            String userType = getUserTypeOrHandleError(eventRequest.organizerId());
            if (userType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The specified organizer does not exist.");
            }

            // Check attendee limits
            if (eventService.isAllowedToCreateEvent(userType, eventRequest.expectedAttendees())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You have exceeded the allowed number of attendees for your role.");
            }

            // Validate room existence and capacity
            if (!roomClient.checkRoomExists(eventRequest.roomId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The specified room does not exist.");
            }
            if (!roomClient.isCapacitySufficient(eventRequest.roomId(), eventRequest.expectedAttendees())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The selected room does not have enough capacity for the expected number of attendees.");
            }

            // Validate room availability via BookingClient
            ResponseEntity<Boolean> roomAvailabilityResponse = bookingClient.isRoomAvailable(
                    eventRequest.roomId(),
                    eventRequest.startTime().toString(),
                    eventRequest.endTime().toString()
            );

            if (!roomAvailabilityResponse.getStatusCode().is2xxSuccessful() || Boolean.FALSE.equals(roomAvailabilityResponse.getBody())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("The room is already booked for the requested time.");
            }

            eventServiceImpl.setEventType(eventRequest.eventType());
            eventServiceImpl.setExpectedAttendees(eventRequest.expectedAttendees());


            // Create booking via BookingClient
            BookingRequest bookingRequest = new BookingRequest(
                    eventRequest.organizerId(),
                    eventRequest.roomId(),
                    eventRequest.startTime(),
                    eventRequest.endTime(),
                    eventRequest.eventName()
            );



            ResponseEntity<BookingResponse> bookingResponseEntity = bookingClient.createEventBooking(bookingRequest);
            if (!bookingResponseEntity.getStatusCode().is2xxSuccessful() || bookingResponseEntity.getBody() == null) {
                String failureMessage = bookingResponseEntity.getBody() != null
                        ? bookingResponseEntity.getBody().purpose()
                        : "Booking creation failed.";
                return ResponseEntity.status(bookingResponseEntity.getStatusCode())
                        .body(failureMessage);
            }
            BookingResponse bookingResponse = bookingResponseEntity.getBody();
            log.info("Booking created successfully: {}", bookingResponse);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("Booking created successfully. Event will be registered asynchronously.");
        } catch (Exception e) {
            log.error("An error occurred while creating the booking request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("An error occurred while creating the booking request.");
        }
    }


    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String eventId) {
        try {
            EventResponse eventResponse = eventService.getEventById(eventId);
            return ResponseEntity.ok(eventResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<?> updateEvent(
            @PathVariable String eventId,
            @RequestBody EventRequest eventRequest) {
        try {
            EventResponse existingEvent = eventService.getEventById(eventId);
            if (existingEvent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The specified event could not be found for updating.");
            }

            boolean requiresUpdate = !eventRequest.roomId().equals(existingEvent.roomId()) ||
                    !eventRequest.startTime().equals(existingEvent.startTime()) ||
                    !eventRequest.endTime().equals(existingEvent.endTime()) ||
                    eventRequest.expectedAttendees() != existingEvent.expectedAttendees();

            if (!requiresUpdate) {
                HttpHeaders headers = new HttpHeaders();
                headers.add("Location", "/api/events/" + existingEvent.id());
                return ResponseEntity.status(HttpStatus.OK).headers(headers).body(existingEvent);
            }

            String userType = getUserTypeOrHandleError(eventRequest.organizerId());
            if (userType == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The specified organizer does not exist.");
            }

            if (eventService.isAllowedToCreateEvent(userType, eventRequest.expectedAttendees())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You have exceeded the allowed number of attendees for your role.");
            }

            if (!roomClient.checkRoomExists(eventRequest.roomId())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("The specified room does not exist.");
            }

            if (!roomClient.isCapacitySufficient(eventRequest.roomId(), eventRequest.expectedAttendees())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("The selected room does not have enough capacity for the expected number of attendees.");
            }

            ResponseEntity<Boolean> roomAvailabilityResponse = bookingClient.isRoomAvailable(
                    eventRequest.roomId(),
                    eventRequest.startTime().toString(),
                    eventRequest.endTime().toString()
            );

            if (!roomAvailabilityResponse.getStatusCode().is2xxSuccessful() || Boolean.FALSE.equals(roomAvailabilityResponse.getBody())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("The room is already booked for the requested time.");
            }

            BookingRequest bookingRequest = new BookingRequest(
                    eventRequest.organizerId(),
                    eventRequest.roomId(),
                    eventRequest.startTime(),
                    eventRequest.endTime(),
                    eventRequest.eventName()
            );

            ResponseEntity<BookingResponse> bookingResponseEntity = bookingClient.createEventBooking(bookingRequest);
            log.info("Booking response: {}", bookingResponseEntity);

            if (!bookingResponseEntity.getStatusCode().is2xxSuccessful() || bookingResponseEntity.getBody() == null) {
                String failureMessage = bookingResponseEntity.getBody() != null
                        ? bookingResponseEntity.getBody().purpose()
                        : "Booking creation failed.";
                return ResponseEntity.status(bookingResponseEntity.getStatusCode()).body(failureMessage);
            }

            BookingResponse bookingResponse = bookingResponseEntity.getBody();
            eventService.updateEvent(eventId, eventRequest, determineStatus(userType), bookingResponse.id());

            EventResponse updatedEvent = eventService.getEventById(eventId);
            log.info("Updated event: {}", updatedEvent);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/api/events/" + updatedEvent.id());
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(updatedEvent);

        } catch (Exception e) {
            log.error("An error occurred while updating the event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("An error occurred while updating the event. Please check your request and try again.");
        }
    }



    private String determineStatus(String userType) {
        return switch (userType.toUpperCase()) {
            case "STUDENT", "FACULTY", "STAFF" -> "PENDING";
            default -> "UNKNOWN";
        };
    }

    @PutMapping("/{eventId}/status")
    public void updateEventStatus(@PathVariable String eventId, @RequestParam String status) {
        log.info("Received request to update status for event ID: {} to {}", eventId, status);

        eventService.updateStatus(eventId, status);
    }


    @DeleteMapping("/{eventId}")
    public void deleteEvent(@PathVariable String eventId) {
        try {
            log.info("Received request to delete event with ID: {}", eventId);
            eventService.deleteEvent(eventId);
        } catch (Exception e) {
            log.error("Error deleting event with ID {}: {}", eventId, e.getMessage());
        }
    }


    @GetMapping("/{eventId}/exists")
    public boolean exists(@PathVariable String eventId) {
        EventResponse event = eventService.getEventById(eventId);
        return event != null;
    }






    // Helper method to handle user retrieval and check if user exists
    private String getUserTypeOrHandleError(Integer organizerId) {
        try {
            return userClient.getUserType(organizerId);
        } catch (Exception e) {
            return null; // Indicates the user was not found
        }
    }
}

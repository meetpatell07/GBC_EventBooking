package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.client.RoomClient;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.event.BookingEvent;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RoomClient roomClient;
    private final MongoTemplate mongoTemplate;

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;

    private String lastError;

    @Override
    public String getLastError() {
        return lastError;
    }

    private static final BookingResponse NOT_FOUND_RESPONSE = new BookingResponse(null, -1, -1, null, null, "Booking Not Found");

    @Override
    public BookingResponse createBooking(BookingRequest bookingRequest) {
        log.debug("Attempting to create a booking: {}", bookingRequest);

        // Check if room exists (let the exception propagate if it fails)
        boolean roomExists = roomClient.checkRoomExists(bookingRequest.roomId());
        if (!roomExists) {
            log.warn("Room with ID {} does not exist.", bookingRequest.roomId());
            return new BookingResponse(null, null, null, null, null, "Room Not Found");
        }

        // Check room maintenance status (let the exception propagate if it fails)
        boolean isRoomAvailableForBooking = roomClient.getRoomAvailability(bookingRequest.roomId());
        if (!isRoomAvailableForBooking) {
            log.info("Room {} is under maintenance and cannot be booked.", bookingRequest.roomId());
            return new BookingResponse(null, null, null, null, null, "Room under maintenance");
        }

        // Check if room is available for the specified time
        if (!isRoomAvailable(bookingRequest.roomId(), bookingRequest.startTime(), bookingRequest.endTime())) {
            log.info("Room {} is already booked.", bookingRequest.roomId());
            return new BookingResponse(null, null, null, null, null, "Room not available for the requested time");
        }

        // Proceed to create the booking if all checks pass
        Booking booking = Booking.builder()
                .userId(bookingRequest.userId())
                .roomId(bookingRequest.roomId())
                .startTime(bookingRequest.startTime())
                .endTime(bookingRequest.endTime())
                .purpose(bookingRequest.purpose())
                .build();

        bookingRepository.save(booking);

        log.info("Booking created successfully with ID: {}", booking.getId());

        return mapToBookingResponse(booking); // Returns a successful BookingResponse
    }

    @Override
    public BookingResponse createEventBooking(BookingRequest bookingRequest) {
        log.debug("Attempting to create an event booking: {}", bookingRequest);

        // Check if room exists (let the exception propagate if it fails)
        boolean roomExists = roomClient.checkRoomExists(bookingRequest.roomId());
        if (!roomExists) {
            log.warn("Room with ID {} does not exist.", bookingRequest.roomId());
            return new BookingResponse(null, null, null, null, null, "Room Not Found");
        }

        // Check room maintenance status (let the exception propagate if it fails)
        boolean isRoomAvailableForBooking = roomClient.getRoomAvailability(bookingRequest.roomId());
        if (!isRoomAvailableForBooking) {
            log.info("Room {} is under maintenance and cannot be booked.", bookingRequest.roomId());
            return new BookingResponse(null, null, null, null, null, "Room under maintenance");
        }

        // Proceed to create the booking if all checks pass
        Booking booking = Booking.builder()
                .userId(bookingRequest.userId())
                .roomId(bookingRequest.roomId())
                .startTime(bookingRequest.startTime())
                .endTime(bookingRequest.endTime())
                .purpose(bookingRequest.purpose())
                .build();

        bookingRepository.save(booking);
        BookingEvent bookingEvent = new BookingEvent( booking.getId(), bookingRequest.roomId(),
                bookingRequest.userId(), bookingRequest.startTime(), bookingRequest.endTime(), bookingRequest.purpose());
        log.info("Start - Sending BookingEvent {} to Kafka topic booking", bookingEvent);
        kafkaTemplate.send("booking", bookingEvent);
        log.info("Complete - Sent BookingEvent {} to Kafka topic booking", bookingEvent);

        return mapToBookingResponse(booking); // Returns a successful BookingResponse
    }

    @Override
    public List<BookingResponse> getAllBookings() {
        log.debug("Fetching all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream().map(this::mapToBookingResponse).toList();
    }

    @Override
    public List<BookingResponse> getBookingsByUserId(Integer userId) {
        log.debug("Fetching all bookings for user with ID {}", userId);
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream().map(this::mapToBookingResponse).toList();
    }

    @Override
    public BookingResponse getBooking(String bookingId) {
        log.debug("Fetching booking with ID {}", bookingId);
        return bookingRepository.findById(bookingId)
                .map(this::mapToBookingResponse)
                .orElse(NOT_FOUND_RESPONSE);
    }

    @Override
    public BookingResponse updateBooking(String id, BookingRequest bookingRequest) {
        log.debug("Updating booking with ID {}", id);

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            lastError = "Booking not found";
            log.info(lastError);
            return null;
        }

        boolean requiresRoomCheck = false;

        if (!booking.getRoomId().equals(bookingRequest.roomId())) {
            booking.setRoomId(bookingRequest.roomId());
            requiresRoomCheck = true;
        }

        if (!booking.getStartTime().equals(bookingRequest.startTime()) ||
                !booking.getEndTime().equals(bookingRequest.endTime())) {
            booking.setStartTime(bookingRequest.startTime());
            booking.setEndTime(bookingRequest.endTime());
            requiresRoomCheck = true;
        }

        if (requiresRoomCheck) {
            boolean isRoomAvailableForBooking = roomClient.getRoomAvailability(bookingRequest.roomId());
            if (!isRoomAvailableForBooking) {
                lastError = "Room under maintenance";
                log.info(lastError);
                return null;
            }

            if (!isRoomAvailable(bookingRequest.roomId(), bookingRequest.startTime(), bookingRequest.endTime())) {
                lastError = "Room not available for the requested time";
                log.info(lastError);
                return null;
            }
        }

        if (!booking.getUserId().equals(bookingRequest.userId())) {
            booking.setUserId(bookingRequest.userId());
        }

        if (!booking.getPurpose().equals(bookingRequest.purpose())) {
            booking.setPurpose(bookingRequest.purpose());
        }

        // Save updated booking only if there are actual changes
        bookingRepository.save(booking);
        log.info("Updated booking with ID {}", id);

        // Clear lastError since the operation was successful
        lastError = null;
        return mapToBookingResponse(booking);
    }

    @Override
    public boolean deleteBooking(String id) {
        log.debug("Deleting booking with ID {}", id);
        if (bookingRepository.existsById(id)) {
            bookingRepository.deleteById(id);
            log.info("Deleted booking with ID {}", id);
            return true;
        } else {
            log.info("Booking with ID {} not found; deletion aborted.", id);
            return false;
        }
    }

    @Override
    public boolean isRoomAvailable(Integer roomId, LocalDateTime startTime, LocalDateTime endTime) {
        Query query = new Query();
        query.addCriteria(Criteria.where("roomId").is(roomId)
                .andOperator(
                        Criteria.where("endTime").gt(startTime),
                        Criteria.where("startTime").lt(endTime)
                ));
        boolean available = mongoTemplate.find(query, Booking.class).isEmpty();
        log.debug("Room availability check for room ID {}: {}", roomId, available ? "Available" : "Not Available");
        return available;
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getRoomId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getPurpose()
        );
    }
}

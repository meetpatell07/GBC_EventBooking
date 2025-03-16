package ca.gbc.bookingservice.controller;

import ca.gbc.bookingservice.client.UserClient;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;
import ca.gbc.bookingservice.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserClient userClient;

    // Create a new booking
    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestBody BookingRequest bookingRequest) {
        // Let the exception propagate for the user type check
        String userType = userClient.getUserType(bookingRequest.userId());

        // If booking service throws an exception, it will be handled by the circuit breaker in the service layer
        BookingResponse createdBooking = bookingService.createBooking(bookingRequest);

        if (createdBooking.id() == null) {
            String failureMessage = createdBooking.purpose();

            return switch (failureMessage) {
                case "Room Not Found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found.");
                case "Room under maintenance" -> ResponseEntity.status(HttpStatus.CONFLICT).body("Room is under maintenance and cannot be booked.");
                case "Room not available for the requested time" -> ResponseEntity.status(HttpStatus.CONFLICT).body("Room is not available for the requested time.");
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking creation failed: " + failureMessage);
            };
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/bookings/" + createdBooking.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdBooking);
    }

    @PostMapping("/event")
    public ResponseEntity<Object> createEventBooking(@RequestBody BookingRequest bookingRequest) {
        // Let the exception propagate for the user type check
        String userType = userClient.getUserType(bookingRequest.userId());

        // If booking service throws an exception, it will be handled by the circuit breaker in the service layer
        BookingResponse createdBooking = bookingService.createEventBooking(bookingRequest);

        if (createdBooking.id() == null) {
            String failureMessage = createdBooking.purpose();

            return switch (failureMessage) {
                case "Room Not Found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found.");
                case "Room under maintenance" -> ResponseEntity.status(HttpStatus.CONFLICT).body("Room is under maintenance and cannot be booked.");
                case "Room not available for the requested time" -> ResponseEntity.status(HttpStatus.CONFLICT).body("Room is not available for the requested time.");
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking creation failed: " + failureMessage);
            };
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/bookings/" + createdBooking.id());
        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdBooking);
    }

    // Retrieve all bookings
    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // Retrieve bookings by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserId(@PathVariable Integer userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }

    // Retrieve a specific booking by ID
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable("bookingId") String bookingId) {
        BookingResponse bookingResponse = bookingService.getBooking(bookingId);
        if (bookingResponse == null || bookingResponse.id() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(bookingResponse);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<Object> updateBooking(
            @PathVariable("bookingId") String bookingId,
            @RequestBody BookingRequest bookingRequest) {

        // Let the exception propagate for the user type check
        String userType = userClient.getUserType(bookingRequest.userId());

        BookingResponse updatedBooking = bookingService.updateBooking(bookingId, bookingRequest);

        if (updatedBooking == null) {
            String failureMessage = bookingService.getLastError();

            return switch (failureMessage) {
                case "Booking not found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found.");
                case "Room not found" -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found.");
                case "Room under maintenance" -> ResponseEntity.status(HttpStatus.CONFLICT).body("Room is under maintenance and cannot be booked.");
                case "Room not available for the requested time" -> ResponseEntity.status(HttpStatus.CONFLICT).body("Room is not available for the requested time.");
                default -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Booking update failed.");
            };
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "/api/bookings/" + updatedBooking.id());
        return ResponseEntity.ok().headers(headers).body(updatedBooking);
    }

    // Delete a booking
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("bookingId") String bookingId) {
        boolean deleted = bookingService.deleteBooking(bookingId);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    public ResponseEntity<Boolean> isRoomAvailable(
            @RequestParam("roomId") Integer roomId,
            @RequestParam("startTime") LocalDateTime startTime,
            @RequestParam("endTime") LocalDateTime endTime) {
        boolean available = bookingService.isRoomAvailable(roomId, startTime, endTime);
        return ResponseEntity.ok(available);
    }
}

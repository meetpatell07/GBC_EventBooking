package ca.gbc.bookingservice.service;

import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.dto.BookingResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest bookingRequest);
    BookingResponse createEventBooking(BookingRequest bookingRequest);
    List<BookingResponse> getAllBookings();
    List<BookingResponse> getBookingsByUserId(Integer userId);
    BookingResponse getBooking(String bookingId);
    BookingResponse updateBooking(String id, BookingRequest bookingRequest);
    boolean deleteBooking(String id);
    boolean isRoomAvailable(Integer roomId, LocalDateTime startTime, LocalDateTime endTime);

    String getLastError();
}

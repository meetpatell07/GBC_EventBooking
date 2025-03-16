package ca.gbc.eventservice.client;

import ca.gbc.eventservice.dto.BookingRequest;
import ca.gbc.eventservice.dto.BookingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "booking", url = "${booking.service.url}")
public interface BookingClient {

    @PostMapping("/api/bookings/event")
    ResponseEntity<BookingResponse> createEventBooking(@RequestBody BookingRequest bookingRequest);

    @DeleteMapping("/api/bookings/{bookingId}")
    ResponseEntity<Void> deleteBooking(@PathVariable("bookingId") String bookingId);

    @RequestMapping(method = RequestMethod.GET, value = "/api/bookings/availability")
    ResponseEntity<Boolean> isRoomAvailable(
            @RequestParam("roomId") Integer roomId,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime);

}

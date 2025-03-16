package ca.gbc.bookingservice.dto;

import java.time.LocalDateTime;

public record BookingResponse(
        String id,
        Integer userId,
        Integer roomId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String purpose
) {
}

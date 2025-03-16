package ca.gbc.eventservice.dto;

import java.time.LocalDateTime;

public record BookingResponse(
        String id,
        Integer userId,         // ID of the user making the booking
        Integer roomId,         // ID of the room to be booked
        LocalDateTime startTime, // Start time of the booking
        LocalDateTime endTime,   // End time of the booking
        String purpose
) {
}

package ca.gbc.eventservice.dto;

import java.time.LocalDateTime;

public record BookingRequest(
        Integer userId,          // ID of the user who made the booking
        Integer roomId,          // ID of the room booked
        LocalDateTime startTime, // Start time of the booking
        LocalDateTime endTime,   // End time of the booking
        String purpose
) {
}

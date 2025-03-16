package ca.gbc.eventservice.dto;
import java.time.LocalDateTime;

public record EventResponse(String id,
                            String eventName,
                            Integer organizerId,
                            String eventType,
                            Integer expectedAttendees,
                            LocalDateTime startTime,
                            LocalDateTime endTime,
                            Integer roomId,
                            String bookingId,
                            String status) {
}

package ca.gbc.eventservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EventRequest(
                           String eventName,
                           Integer organizerId,
                           String eventType,
                           Integer expectedAttendees,
                           LocalDateTime startTime,
                           LocalDateTime endTime,
                           Integer roomId
) {

}

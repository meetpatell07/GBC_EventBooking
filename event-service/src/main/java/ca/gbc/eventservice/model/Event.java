package ca.gbc.eventservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(collection = "event")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {

    @Id
    private String id;

    private String eventName;

    private Integer organizerId;  // ID reference to the user organizing the event

    private String eventType;  // e.g., "Conference", "Workshop", etc.

    private Integer expectedAttendees; // Optional, for estimated attendance count
    
    private LocalDateTime startTime; // Start time of the event

    private LocalDateTime endTime;   // End time for scheduling accuracy

    private String bookingId;   // ID of the booking created in BookingService


    private Integer roomId;  // ID reference to the room where the event will be held
    
    private String status;  // Pending, approved,
}

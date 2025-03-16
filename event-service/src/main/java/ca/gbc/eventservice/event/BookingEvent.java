package ca.gbc.eventservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEvent {
    private String bookingId;
    private Integer roomId;
    private Integer userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;

}

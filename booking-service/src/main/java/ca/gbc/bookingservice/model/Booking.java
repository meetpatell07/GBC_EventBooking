package ca.gbc.bookingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "booking")
@Builder
public class Booking {

    @Id
    private String id;
    private Integer userId;
    private Integer roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;

}

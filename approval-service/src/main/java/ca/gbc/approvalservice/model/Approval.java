package ca.gbc.approvalservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "approvals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Approval {
    @Id
    private String id; // Approval

    private String eventId;      // Link to the event awaiting approval
    private Integer reviewerId;  // Basically, UserId of approval service
    private String status;       // Approval status: "APPROVED" or "REJECTED"
    private String comments;     // Optional comments from the reviewer
}

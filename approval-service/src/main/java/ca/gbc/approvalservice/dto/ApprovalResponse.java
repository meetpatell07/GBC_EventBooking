package ca.gbc.approvalservice.dto;

public record ApprovalResponse(
        String id,           // Unique ID of the approval record
        String eventId,      // ID of the event linked to the approval
        Integer reviewerId,  // ID of the staff member who reviewed the event
        String status,       // Approval status: "PENDING", "APPROVED", "REJECTED"
        String comments

) {
}

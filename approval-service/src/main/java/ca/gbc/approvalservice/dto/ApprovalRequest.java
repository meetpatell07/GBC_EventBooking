package ca.gbc.approvalservice.dto;

public record ApprovalRequest(
        String eventId,
        Integer reviewerId,  // ID of the reviewer (must be an ADMIN or SUPERADMIN)
        String status,
        String comments
) {
}

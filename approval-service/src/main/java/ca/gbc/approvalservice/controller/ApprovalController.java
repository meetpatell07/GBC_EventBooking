package ca.gbc.approvalservice.controller;

import ca.gbc.approvalservice.client.EventClient;
import ca.gbc.approvalservice.client.UserClient;
import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import org.springframework.web.client.RestClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final UserClient userClient;
    private final EventClient eventClient;

    @GetMapping
    public ResponseEntity<List<ApprovalResponse>> getAllApprovals() {
        List<ApprovalResponse> approvals = approvalService.getAllApprovals();
        return new ResponseEntity<>(approvals, HttpStatus.OK);
    }


    @PostMapping("/process")
    public ResponseEntity<ApprovalResponse> processApproval(@RequestBody ApprovalRequest request) {

        // Validate the reviewer role
        String reviewerRole;
        try {
            reviewerRole = userClient.getUserRole(request.reviewerId());
        } catch (RestClientException e) {
            if (e.getMessage().contains("404")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApprovalResponse(null, request.eventId(), request.reviewerId(), "PENDING", "Reviewer not found"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApprovalResponse(null, request.eventId(), request.reviewerId(), "PENDING", "Error while validating reviewer role"));
        }

        if ("USER".equalsIgnoreCase(reviewerRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApprovalResponse(null, request.eventId(), request.reviewerId(), "PENDING", "Only ADMIN or SUPERADMIN can approve or reject events"));
        }
        try {
            eventClient.checkEventExists(request.eventId());
        } catch (RestClientException e) {
            if (e.getMessage().contains("404")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApprovalResponse(null, request.eventId(), request.reviewerId(), "PENDING", "Event not found"));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApprovalResponse(null, request.eventId(), request.reviewerId(), "PENDING", "Error while checking event existence"));
        }
        // Call service methods if validations pass
        if ("APPROVED".equalsIgnoreCase(request.status())) {
            return new ResponseEntity<>(approvalService.approveEvent(request.eventId(), request.reviewerId(), request.comments()), HttpStatus.OK);
        } else if ("REJECTED".equalsIgnoreCase(request.status())) {
            return new ResponseEntity<>(approvalService.rejectEvent(request.eventId(), request.reviewerId(), request.comments()), HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApprovalResponse(null, request.eventId(), request.reviewerId(), "PENDING", "Invalid status value"));
        }
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<String> getUserType(@PathVariable Integer userId) {
        String userType;
        try {
            userType = userClient.getUserType(userId);
        } catch (RestClientException e) {
            if (e.getMessage().contains("404")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while fetching user type");
        }
        return new ResponseEntity<>(userType, HttpStatus.OK);
    }

    @GetMapping("/{approvalId}")
    public ResponseEntity<ApprovalResponse> getApprovalById(@PathVariable String approvalId) {
        ApprovalResponse response = approvalService.getApprovalById(approvalId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

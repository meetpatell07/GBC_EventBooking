package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.client.EventClient;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.model.Approval;
import ca.gbc.approvalservice.repository.ApprovalRepository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final EventClient eventClient;

    @Override
    public List<ApprovalResponse> getAllApprovals() {
        return approvalRepository.findAll()
                .stream()
                .map(this::mapToApprovalResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovalResponse approveEvent(String eventId, Integer reviewerId, String comments) {
        // Retrieve the approval record or create a new one if it doesn't exist
        Approval approval = approvalRepository.findByEventId(eventId)
                .orElse(new Approval(null, eventId, reviewerId, "PENDING", null));

        // Update approval status and reviewer
        approval.setStatus("APPROVED");
        approval.setReviewerId(reviewerId);
        approval.setComments(comments);
        approvalRepository.save(approval);

        // Try to update the event status in Event Service
        try {
            eventClient.updateEventStatus(eventId, "APPROVED");
        } catch (HttpClientErrorException.NotFound e) {
            // Log the error details, including status code and message
            log.error("Failed to update event status to APPROVED for event ID {}. Status: {}, Error: {}",
                    eventId, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Event with ID " + eventId + " not found in Event Service.");
        } catch (RestClientException e) {
            log.error("Failed to update event status due to an error with the Event Service: {}", e.getMessage());
            throw new RuntimeException("Failed to update event status due to an error with the Event Service.");
        }

        return mapToApprovalResponse(approval);
    }


    @Override
    public ApprovalResponse rejectEvent(String eventId, Integer reviewerId, String comments) {
        // Create or retrieve approval record
        Approval approval = approvalRepository.findByEventId(eventId)
                .orElse(new Approval(null, eventId, reviewerId, "PENDING", comments));

        // Set to rejected and save
        approval.setStatus("REJECTED");
        approval.setReviewerId(reviewerId);
        approval.setComments(comments);
        approvalRepository.save(approval);
        try {
            log.info("Updating event status to REJECTED for event ID: {}", eventId);
            eventClient.updateEventStatus(eventId, "REJECTED");
            eventClient.deleteEvent(eventId);
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Event not found while rejecting event ID {}. Status: {}, Error: {}", eventId, e.getStatusCode(), e.getMessage());
            throw new RuntimeException("Event with ID " + eventId + " not found in Event Service.");
        } catch (RestClientException e) {
            log.error("Error while rejecting event ID {}. Status: {}, Error: {}", eventId, e.getMessage());
            throw new RuntimeException("Failed to reject event due to an error with the Event Service.");
        }

        return mapToApprovalResponse(approval);
    }



    @Override
    public ApprovalResponse getApprovalById(String approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval record not found with ID: " + approvalId));

        return mapToApprovalResponse(approval);
    }
    private ApprovalResponse mapToApprovalResponse(Approval approval) {
        return new ApprovalResponse(
                approval.getId(),
                approval.getEventId(),
                approval.getReviewerId(),
                approval.getStatus(),
                approval.getComments()
        );
    }
}


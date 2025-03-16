package ca.gbc.approvalservice.service;

import ca.gbc.approvalservice.dto.ApprovalResponse;

import java.util.List;

public interface ApprovalService {

    List<ApprovalResponse> getAllApprovals();
    ApprovalResponse approveEvent(String approvalId, Integer reviewerId,String comments);
    ApprovalResponse rejectEvent(String approvalId, Integer reviewerId, String comments);
    ApprovalResponse getApprovalById(String approvalId);



}

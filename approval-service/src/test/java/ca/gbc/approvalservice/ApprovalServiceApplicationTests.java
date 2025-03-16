package ca.gbc.approvalservice;

import ca.gbc.approvalservice.client.EventClient;
import ca.gbc.approvalservice.client.UserClient;
import ca.gbc.approvalservice.controller.ApprovalController;
import ca.gbc.approvalservice.dto.ApprovalRequest;
import ca.gbc.approvalservice.dto.ApprovalResponse;
import ca.gbc.approvalservice.service.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import feign.Request;
import feign.RequestTemplate;
import feign.Request.HttpMethod;
import java.util.Collections;

@WebMvcTest(ApprovalController.class)
public class ApprovalServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalService approvalService;

    @MockBean
    private UserClient userClient;

    @MockBean
    private EventClient eventClient;

    @Autowired
    private ObjectMapper objectMapper;

    private ApprovalRequest approvalRequest;
    private ApprovalResponse approvalResponse;

    @BeforeEach
    void setup() {
        approvalRequest = new ApprovalRequest("eventId123", 37, "APPROVED", "Looks good");
        approvalResponse = new ApprovalResponse("approvalId123", "eventId123", 37, "APPROVED", "Looks good");
    }

    @Test
    void getAllApprovals_ReturnsApprovalList() throws Exception {
        List<ApprovalResponse> approvals = List.of(approvalResponse);

        Mockito.when(approvalService.getAllApprovals()).thenReturn(approvals);

        mockMvc.perform(get("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(approvalResponse.id())))
                .andExpect(jsonPath("$[0].eventId", is(approvalResponse.eventId())));
    }

    @Test
    void processApproval_ApproveEvent_ReturnsApprovedResponse() throws Exception {
        Mockito.when(userClient.getUserRole(any(Integer.class))).thenReturn("SUPERADMIN");
        Mockito.when(eventClient.checkEventExists(anyString())).thenReturn(true);
        Mockito.when(approvalService.approveEvent(anyString(), any(Integer.class), anyString())).thenReturn(approvalResponse);

        ResultActions result = mockMvc.perform(post("/api/approvals/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")))
                .andExpect(jsonPath("$.comments", is("Looks good")));

        result.andExpect(jsonPath("$.id", is(approvalResponse.id())))
                .andExpect(jsonPath("$.eventId", is(approvalResponse.eventId())))
                .andExpect(jsonPath("$.reviewerId", is(approvalResponse.reviewerId())));
    }

    @Test
    void processApproval_RejectEvent_ReturnsRejectedResponse() throws Exception {
        ApprovalRequest rejectRequest = new ApprovalRequest("eventId123", 37, "REJECTED", "Not enough details");
        ApprovalResponse rejectedResponse = new ApprovalResponse("approvalId123", "eventId123", 37, "REJECTED", "Not enough details");

        Mockito.when(userClient.getUserRole(any(Integer.class))).thenReturn("SUPERADMIN");
        Mockito.when(eventClient.checkEventExists(anyString())).thenReturn(true);
        Mockito.when(approvalService.rejectEvent(anyString(), any(Integer.class), anyString())).thenReturn(rejectedResponse);

        ResultActions result = mockMvc.perform(post("/api/approvals/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")))
                .andExpect(jsonPath("$.comments", is("Not enough details")));

        result.andExpect(jsonPath("$.id", is(rejectedResponse.id())))
                .andExpect(jsonPath("$.eventId", is(rejectedResponse.eventId())))
                .andExpect(jsonPath("$.reviewerId", is(rejectedResponse.reviewerId())));
    }

    @Test
    void processApproval_InvalidStatus_ReturnsBadRequest() throws Exception {
        ApprovalRequest invalidRequest = new ApprovalRequest("eventId123", 37, "INVALID", "Invalid status");

        mockMvc.perform(post("/api/approvals/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.comments", is("Invalid status value")));
    }

    @Test
    void processApproval_UserNotAuthorized_ReturnsForbidden() throws Exception {
        Mockito.when(userClient.getUserRole(any(Integer.class))).thenReturn("USER");

        mockMvc.perform(post("/api/approvals/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.comments", is("Only ADMIN or SUPERADMIN can approve or reject events")));
    }

    @Test
    void processApproval_EventNotFound_ReturnsNotFound() throws Exception {
        // Mock the user role to return SUPERADMIN
        Mockito.when(userClient.getUserRole(any(Integer.class))).thenReturn("SUPERADMIN");

        // Create a minimal Request object to pass to FeignException.NotFound
        Request request = Request.create(HttpMethod.GET, "/api/events/eventId123/exists", Collections.emptyMap(), Request.Body.empty(), new RequestTemplate());

        // Mock eventClient to throw FeignException.NotFound with the request and response body
        Mockito.doThrow(new FeignException.NotFound("Event not found", request, null, Collections.emptyMap()))
                .when(eventClient).checkEventExists(anyString());

        mockMvc.perform(post("/api/approvals/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approvalRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.comments", is("Event not found")));
    }


    @Test
    void getApprovalById_ReturnsApprovalResponse() throws Exception {
        Mockito.when(approvalService.getApprovalById(anyString())).thenReturn(approvalResponse);

        mockMvc.perform(get("/api/approvals/approvalId123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(approvalResponse.id())))
                .andExpect(jsonPath("$.eventId", is(approvalResponse.eventId())))
                .andExpect(jsonPath("$.reviewerId", is(approvalResponse.reviewerId())))
                .andExpect(jsonPath("$.status", is(approvalResponse.status())))
                .andExpect(jsonPath("$.comments", is(approvalResponse.comments())));
    }
}

package ca.gbc.eventservice.service;

import ca.gbc.eventservice.dto.EventRequest;
import ca.gbc.eventservice.dto.EventResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EventService {

    EventResponse createEvent(EventRequest eventRequest, String status, String bookingId);
    List<EventResponse> getAllEvents();
    EventResponse getEventById(String eventId);
    EventResponse updateEvent(String eventId, EventRequest eventRequest, String status, String bookingId);
    void deleteEvent(String eventId);
    boolean isAllowedToCreateEvent(String userType, int expectedAttendees);
    void updateStatus(String eventId, String status);


}

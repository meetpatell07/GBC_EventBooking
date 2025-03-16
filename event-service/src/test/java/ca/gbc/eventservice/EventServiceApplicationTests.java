package ca.gbc.eventservice;

import ca.gbc.eventservice.client.BookingClient;
import ca.gbc.eventservice.client.RoomClient;
import ca.gbc.eventservice.client.UserClient;
import ca.gbc.eventservice.controller.EventController;
import ca.gbc.eventservice.dto.*;
import ca.gbc.eventservice.service.EventService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class EventServiceApplicationTests {

    @Mock
    private EventService eventService;

    @Mock
    private UserClient userClient;

    @Mock
    private RoomClient roomClient;

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        RestAssuredMockMvc.standaloneSetup(eventController);
    }

    @Test
    public void testCreateEvent_Success() {
        EventRequest eventRequest = new EventRequest("Workshop", 1, "Conference", 50,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 101);

        when(userClient.getUserType(1)).thenReturn("FACULTY");
        when(roomClient.checkRoomExists(101)).thenReturn(true);
        when(roomClient.isCapacitySufficient(101, 50)).thenReturn(true);
        when(bookingClient.isRoomAvailable(101, eventRequest.startTime().toString(), eventRequest.endTime().toString()))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        BookingResponse bookingResponse = new BookingResponse("booking123", 1, 101, eventRequest.startTime(), eventRequest.endTime(), "Workshop");
        when(bookingClient.createEventBooking(any(BookingRequest.class))).thenReturn(new ResponseEntity<>(bookingResponse, HttpStatus.CREATED));

        EventResponse eventResponse = new EventResponse("event123", "Workshop", 1, "Conference", 50,
                eventRequest.startTime(), eventRequest.endTime(), 101, "booking123", "PENDING");
        when(eventService.createEvent(any(EventRequest.class), eq("PENDING"), eq("booking123"))).thenReturn(eventResponse);

        given()
                .contentType("application/json")
                .body(eventRequest)
                .when()
                .post("/api/events")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", equalTo("event123"))
                .body("status", equalTo("PENDING"));
    }

    @Test
    public void testGetAllEvents() {
        EventResponse event1 = new EventResponse("event123", "Workshop", 1, "Conference", 50,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 101, "booking123", "PENDING");
        EventResponse event2 = new EventResponse("event456", "Seminar", 2, "Meeting", 30,
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2), 102, "booking456", "APPROVED");

        when(eventService.getAllEvents()).thenReturn(List.of(event1, event2));

        given()
                .contentType("application/json")
                .when()
                .get("/api/events")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("[0].id", equalTo("event123"))
                .body("[1].id", equalTo("event456"));
    }

    @Test
    public void testGetEventById_Success() {
        EventResponse eventResponse = new EventResponse("event123", "Workshop", 1, "Conference", 50,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 101, "booking123", "PENDING");

        when(eventService.getEventById("event123")).thenReturn(eventResponse);

        given()
                .contentType("application/json")
                .when()
                .get("/api/events/event123")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo("event123"))
                .body("status", equalTo("PENDING"));
    }

    @Test
    public void testUpdateEventStatus() {
        doNothing().when(eventService).updateStatus("event123", "APPROVED");

        given()
                .contentType("application/json")
                .when()
                .put("/api/events/event123/status?status=APPROVED")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(eventService, times(1)).updateStatus("event123", "APPROVED");
    }

    @Test
    public void testDeleteEvent_Success() {
        doNothing().when(eventService).deleteEvent("event123");

        given()
                .contentType("application/json")
                .when()
                .delete("/api/events/event123")
                .then()
                .statusCode(HttpStatus.OK.value());

        verify(eventService, times(1)).deleteEvent("event123");
    }

    @Test
    public void testExists_Success() {
        EventResponse eventResponse = new EventResponse("event123", "Workshop", 1, "Conference", 50,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 101, "booking123", "PENDING");

        when(eventService.getEventById("event123")).thenReturn(eventResponse);

        given()
                .contentType("application/json")
                .when()
                .get("/api/events/event123/exists")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body(equalTo("true"));
    }

    @Test
    public void testUpdateEvent_Success() {
        EventRequest eventRequest = new EventRequest("Workshop Update", 1, "Conference", 50,
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(3).plusHours(2), 101);
        EventResponse existingEvent = new EventResponse("event123", "Workshop", 1, "Conference", 50,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 101, "booking123", "PENDING");

        when(eventService.getEventById("event123")).thenReturn(existingEvent);
        when(userClient.getUserType(1)).thenReturn("FACULTY");
        when(roomClient.checkRoomExists(101)).thenReturn(true);
        when(roomClient.isCapacitySufficient(101, 50)).thenReturn(true);
        when(bookingClient.isRoomAvailable(101, eventRequest.startTime().toString(), eventRequest.endTime().toString()))
                .thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        BookingResponse bookingResponse = new BookingResponse("newBookingId", 1, 101, eventRequest.startTime(), eventRequest.endTime(), "Workshop Update");
        when(bookingClient.createEventBooking(any(BookingRequest.class))).thenReturn(new ResponseEntity<>(bookingResponse, HttpStatus.CREATED));

        EventResponse updatedEvent = new EventResponse("event123", "Workshop Update", 1, "Conference", 50,
                eventRequest.startTime(), eventRequest.endTime(), 101, "newBookingId", "PENDING");
        when(eventService.updateEvent(anyString(), any(EventRequest.class), anyString(), anyString())).thenReturn(updatedEvent);

        given()
                .contentType("application/json")
                .body(eventRequest)
                .when()
                .put("/api/events/event123")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("id", equalTo("event123"))
                .body("status", equalTo("PENDING"));
    }
}

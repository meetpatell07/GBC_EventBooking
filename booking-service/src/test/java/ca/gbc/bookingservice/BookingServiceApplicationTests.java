package ca.gbc.bookingservice;

import ca.gbc.bookingservice.client.RoomClient;
import ca.gbc.bookingservice.client.UserClient;
import ca.gbc.bookingservice.dto.BookingRequest;
import ca.gbc.bookingservice.model.Booking;
import ca.gbc.bookingservice.repository.BookingRepository;
import ca.gbc.bookingservice.service.BookingService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;



import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class BookingServiceIntegrationTest {

	@Value("${local.server.port}")
	private int port;

	@MockBean
	private RoomClient roomClient;

	@Autowired
	private BookingService bookingService;

	@MockBean
	private UserClient userClient;

	@Autowired
	private BookingRepository bookingRepository;


	@Autowired
	private MongoTemplate mongoTemplate;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		RestAssured.port = port;
		RestAssured.baseURI = "http://localhost";
		bookingRepository.deleteAll();
	}

	@Test
	void mockSetupTest() {
		when(roomClient.getRoomAvailability(101)).thenReturn(true);

		// Verify that the mock works as expected
		boolean isAvailable = roomClient.getRoomAvailability(101);
		assertTrue(isAvailable);
	}

	@Test
	void createBookingTest() {
		// Arrange: Mock RoomClient responses for room existence and availability
		when(roomClient.checkRoomExists(101)).thenReturn(true);  // Mock room exists
		when(roomClient.getRoomAvailability(101)).thenReturn(true);  // Mock room is available

		// Booking request payload
		String createBookingRequest = """
				{
				  "userId": 37,
				  "roomId": 101,
				  "startTime": "2024-11-10T10:00:00",
				  "endTime": "2024-11-10T12:00:00",
				  "purpose": "Workshop"
				}
				""";

		// Act: Send POST request to create the booking
		given()
				.contentType("application/json")
				.body(createBookingRequest)
				.when()
				.post("/api/bookings")
				.then()
				.statusCode(201)  // Expecting 201 Created
				.body("userId", equalTo(37))
				.body("roomId", equalTo(101))
				.body("purpose", equalTo("Workshop"));

		// Verify that the room existence and availability checks were called
		verify(roomClient, times(1)).checkRoomExists(101);
		verify(roomClient, times(1)).getRoomAvailability(101);
	}


	@Test
	void createBooking_RoomUnavailable() {
		BookingRequest bookingRequest = new BookingRequest(
				37,
				103,
				LocalDateTime.now().plusDays(1),
				LocalDateTime.now().plusDays(1).plusHours(2),
				"Conference"
		);

		when(roomClient.checkRoomExists(103)).thenReturn(true);
		when(roomClient.getRoomAvailability(103)).thenReturn(false);

		given()
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.body(bookingRequest)
				.when()
				.post("/api/bookings")
				.then()
				.statusCode(HttpStatus.CONFLICT.value())
				.body(equalTo("Room is under maintenance and cannot be booked."));

		verify(roomClient, times(1)).checkRoomExists(103);
		verify(roomClient, times(1)).getRoomAvailability(103);
	}

	@Test
	void getAllBookings_Success() {
		Booking booking1 = new Booking("1", 37, 101, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Workshop");
		Booking booking2 = new Booking("2", 38, 102, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3), "Meeting");

		bookingRepository.saveAll(List.of(booking1, booking2));

		given()
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.when()
				.get("/api/bookings")
				.then()
				.statusCode(HttpStatus.OK.value())
				.body("size()", equalTo(2))
				.body("[0].purpose", equalTo("Workshop"))
				.body("[1].purpose", equalTo("Meeting"));
	}

	@Test
	void getBookingById_Success() {
		Booking booking = new Booking("1", 37, 101, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "Workshop");
		bookingRepository.save(booking);

		given()
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.when()
				.get("/api/bookings/{id}", "1")
				.then()
				.statusCode(HttpStatus.OK.value())
				.body("id", equalTo("1"))
				.body("purpose", equalTo("Workshop"));
	}

	@Test
	void updateBookingTest() {
		// Arrange: Initial booking creation setup
		String createBookingRequest = """
        {
          "userId": 37,
          "roomId": 101,
          "startTime": "2024-11-10T10:00:00",
          "endTime": "2024-11-10T12:00:00",
          "purpose": "Workshop"
        }
        """;

		// Mock RoomClient for room existence and availability checks
		when(roomClient.checkRoomExists(101)).thenReturn(true); // Room exists
		when(roomClient.getRoomAvailability(101)).thenReturn(true); // Room is available

		// Step 1: Create a booking and extract its ID
		String bookingId = given()
				.contentType("application/json")
				.body(createBookingRequest)
				.when()
				.post("/api/bookings")
				.then()
				.statusCode(201)
				.extract()
				.path("id");

		// Step 2: Prepare update booking request with modified purpose
		String updateBookingRequest = """
        {
          "userId": 37,
          "roomId": 101,
          "startTime": "2024-11-11T14:00:00",
          "endTime": "2024-11-11T16:00:00",
          "purpose": "Updated Workshop"
        }
        """;

		// Act: Send PUT request to update the booking
		given()
				.contentType("application/json")
				.body(updateBookingRequest)
				.when()
				.put("/api/bookings/" + bookingId)
				.then()
				.statusCode(200) // Expecting 200 OK
				.body("id", equalTo(bookingId))
				.body("purpose", equalTo("Updated Workshop"))
				.body("userId", equalTo(37))
				.body("roomId", equalTo(101));

		// Verify that room availability was checked
		verify(roomClient, times(1)).checkRoomExists(101); // Confirm existence only once
		verify(roomClient, times(2)).getRoomAvailability(101); // Once during creation, once during update
	}


}
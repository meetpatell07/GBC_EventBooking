package ca.gbc.roomservice;

import ca.gbc.roomservice.repository.RoomRepository;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class RoomServiceApplicationTests {
	@ServiceConnection
	static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
			.withDatabaseName("testdb")
			.withUsername("testuser")
			.withPassword("testpass");

	@LocalServerPort
	private Integer port;

	@Autowired
	private RoomRepository roomRepository;

	@DynamicPropertySource
	static void registerPostgresProperties(DynamicPropertyRegistry registry) {
		postgreSQLContainer.start();
		registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
		registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
		registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
	}

	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
		roomRepository.deleteAll();  // Clear the database before each test
	}

	@Test
	void createRoomTest() {
		String requestBody = """
            {
                "name": "Room A",
                "capacity": 50,
                "availability": true,
                "features": ["Projector", "Whiteboard"]
            }
        """;

		given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/room")
				.then()
				.log().ifError()
				.statusCode(201)
				.body("id", Matchers.notNullValue())
				.body("name", equalTo("Room A"))
				.body("capacity", equalTo(50))
				.body("availability", equalTo(true))
				.body("features", Matchers.contains("Projector", "Whiteboard"));
	}

	@Test
	void updateRoomTest() {
		String createRoomRequest = """
            {
              "name": "Room 101",
              "capacity": 20,
              "features": ["Projector", "Whiteboard"],
              "availability": true
            }
            """;

		int roomId = given()
				.contentType("application/json")
				.body(createRoomRequest)
				.when()
				.post("/api/room")
				.then()
				.statusCode(201)
				.extract()
				.path("id");

		String updateRoomRequest = """
            {
              "name": "Updated Room",
              "capacity": 25,
              "features": ["Projector", "Whiteboard", "Video Conferencing"],
              "availability": false
            }
            """;

		given()
				.contentType("application/json")
				.body(updateRoomRequest)
				.when()
				.put("/api/room/" + roomId)
				.then()
				.statusCode(200) // Expecting 200 instead of 204
				.body("message", equalTo("Room with ID " + roomId + " was successfully updated."));
	}

	@Test
	void getAllRoomsTest() {
		String requestBody = """
            {
                "name": "Room C",
                "capacity": 25,
                "availability": true,
                "features": ["Microphone"]
            }
        """;

		given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/room")
				.then()
				.statusCode(201);

		given()
				.contentType("application/json")
				.when()
				.get("/api/room")
				.then()
				.log().ifError()
				.statusCode(200)
				.body("size()", Matchers.greaterThan(0))
				.body("[0].name", equalTo("Room C"));
	}

	@Test
	void deleteRoomTest() {
		String createRoomRequest = """
            {
              "name": "Room D",
              "capacity": 20,
              "features": ["Projector", "Whiteboard"],
              "availability": true
            }
            """;

		int roomId = given()
				.contentType("application/json")
				.body(createRoomRequest)
				.when()
				.post("/api/room")
				.then()
				.statusCode(201)
				.extract()
				.path("id");

		given()
				.when()
				.delete("/api/room/" + roomId)
				.then()
				.statusCode(204); // Expecting 204 for successful deletion
	}

	@Test
	void getRoomAvailabilityTest() {
		String requestBody = """
            {
                "name": "Room E",
                "capacity": 40,
                "availability": true,
                "features": ["Projector"]
            }
        """;

		int roomId = given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/room")
				.then()
				.statusCode(201)
				.extract()
				.path("id");

		given()
				.when()
				.get("/api/room/" + roomId + "/availability")
				.then()
				.statusCode(200)
				.body(equalTo("true"));
	}

	@Test
	void getRoomCapacityTest() {
		String requestBody = """
            {
                "name": "Room F",
                "capacity": 35,
                "availability": false,
                "features": ["Whiteboard"]
            }
        """;

		int roomId = given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/room")
				.then()
				.statusCode(201)
				.extract()
				.path("id");

		given()
				.when()
				.get("/api/room/" + roomId + "/capacity")
				.then()
				.statusCode(200)
				.body(equalTo("35"));
	}

	@Test
	void checkRoomExistsTest() {
		String requestBody = """
            {
                "name": "Room G",
                "capacity": 45,
                "availability": true,
                "features": ["Speaker"]
            }
        """;

		int roomId = given()
				.contentType("application/json")
				.body(requestBody)
				.when()
				.post("/api/room")
				.then()
				.statusCode(201)
				.extract()
				.path("id");

		given()
				.when()
				.get("/api/room/" + roomId + "/exists")
				.then()
				.statusCode(200)
				.body(equalTo("true"));
	}


}
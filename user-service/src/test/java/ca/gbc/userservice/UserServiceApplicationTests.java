package ca.gbc.userservice;

import ca.gbc.userservice.repository.UserRepository;
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
import io.restassured.RestAssured;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceIntegrationTests {

    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @LocalServerPort
    private Integer port;

    @Autowired
    private UserRepository userRepository;

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

        // Clear the database before each test to avoid duplicate entries
        userRepository.deleteAll();
    }

    @Test
    void createUserTest() {
        String requestBody = """
                    {
                        "name": "John Doe",
                        "email": "johndoe@example.com",
                        "role": "USER",
                        "userType": "STUDENT"
                    }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/users")
                .then()
                .log().ifError()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("John Doe"))
                .body("email", Matchers.equalTo("johndoe@example.com"))
                .body("role", Matchers.equalTo("USER"))
                .body("userType", Matchers.equalTo("STUDENT"));
    }

    @Test
    void getAllUsersTest() {
        // First, create a user to ensure there’s data to retrieve
        String requestBody = """
                    {
                        "name": "John Doe",
                        "email": "johndoe_unique@example.com",
                        "role": "USER",
                        "userType": "STUDENT"
                    }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/users")
                .then()
                .log().ifError()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name", Matchers.equalTo("John Doe"))
                .body("email", Matchers.equalTo("johndoe_unique@example.com"))
                .body("role", Matchers.equalTo("USER"))
                .body("userType", Matchers.equalTo("STUDENT"));

        // Retrieve all users and verify the created user is present
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/users")
                .then()
                .log().ifError()
                .statusCode(200)
                .body("size()", Matchers.greaterThan(0))
                .body("[0].name", Matchers.equalTo("John Doe"))
                .body("[0].email", Matchers.equalTo("johndoe_unique@example.com"))
                .body("[0].role", Matchers.equalTo("USER"))
                .body("[0].userType", Matchers.equalTo("STUDENT"));
    }

    @Test
    void updateUserTest() {
        // First, create a user
        String requestBody = """
                    {
                        "name": "John Doe",
                        "email": "johndoe@example.com",
                        "role": "USER",
                        "userType": "STUDENT"
                    }
                """;

        int userId = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Define updated information for the user
        String updateRequestBody = """
                    {
                        "name": "John Doe Updated",
                        "email": "johndoe@example.com",
                        "role": "ADMIN",
                        "userType": "STAFF"
                    }
                """;

        // Send the update request and check the status code only
        RestAssured.given()
                .contentType("application/json")
                .body(updateRequestBody)
                .when()
                .put("/api/users/" + userId)
                .then()
                .log().ifError()
                .statusCode(200);

        // Fetch the updated user to verify changes
        RestAssured.given()
                .contentType("application/json")
                .when()
                .get("/api/users/" + userId)
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("John Doe Updated"))
                .body("role", Matchers.equalTo("ADMIN"))
                .body("userType", Matchers.equalTo("STAFF"));
    }


    @Test
    void deleteUserTest() {
        // Create user first
        String requestBody = """
                    {
                        "name": "John Doe",
                        "email": "johndoe@example.com",
                        "role": "USER",
                        "userType": "STUDENT"
                    }
                """;

        int userId = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/users")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        // Delete the user
        RestAssured.given()
                .when()
                .delete("/api/users/" + userId) // Assuming /api/users/{id} for delete
                .then()
                .log().ifError()
                .statusCode(204); // 204 No Content for a successful delete

        // Verify the user no longer exists
        RestAssured.given()
                .when()
                .get("/api/users/" + userId)
                .then()
                .log().ifError()
                .statusCode(404); // 404 Not Found, indicating the user was deleted
    }
}
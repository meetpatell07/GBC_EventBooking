package ca.gbc.userservice.controller;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get all users
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}/role")
    public ResponseEntity<String> getUserRole(@PathVariable Integer userId) {
        String userRole = userService.getUserRole(userId);
        if ("UNKNOWN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(userRole);
    }

    // Retrieve user type by userId
    @GetMapping("/{userId}/type")
    public ResponseEntity<String> getUserType(@PathVariable Integer userId) {
        String userType = userService.getUserType(userId);
        if ("UNKNOWN".equals(userType)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(userType);
    }

    // Create a new user
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest userRequest) {
        String validationMessage = validateUserRequest(userRequest);
        if (!validationMessage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationMessage);
        }
        UserResponse createdUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // Update an existing user by ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id, @RequestBody UserRequest userRequest) {
        String validationMessage = validateUserRequest(userRequest);
        if (!validationMessage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationMessage);
        }
        if (!userService.userExists(id)){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "User with ID " + id + " does not exist."));
        }
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        String message = "User with ID " + id + " was successfully updated.";

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Collections.singletonMap("message", message));
    }

    // Delete a user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        if (!userService.userExists(id)){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "User with ID " + id + " does not exist."));
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Helper method for validating UserRequest
    private String validateUserRequest(UserRequest userRequest) {
        if (userRequest.name() == null || userRequest.name().isBlank()) {
            return "Name is required";
        }
        if (userRequest.email() == null || userRequest.email().isBlank()) {
            return "Email is required";
        }
        if (userRequest.role() == null) {
            return "Role is required";
        }
        if (userRequest.userType() == null) {
            return "UserType is required";
        }
        return ""; // No validation errors
    }
}

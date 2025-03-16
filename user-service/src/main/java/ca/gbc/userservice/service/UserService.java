package ca.gbc.userservice.service;

import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserResponse> getAllUsers();
    Optional<UserResponse> getUserById(Integer id);
    UserResponse createUser(UserRequest userRequest);
    UserResponse updateUser(Integer id, UserRequest userRequest);
    void deleteUser(Integer id);
    boolean userExists(Integer userId);
    String getUserType(Integer userId);
    String getUserRole(Integer userId);

}

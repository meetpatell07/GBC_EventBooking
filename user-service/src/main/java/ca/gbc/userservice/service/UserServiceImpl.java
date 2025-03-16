package ca.gbc.userservice.service;
import ca.gbc.userservice.dto.UserRequest;
import ca.gbc.userservice.dto.UserResponse;
import ca.gbc.userservice.model.User;
import ca.gbc.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getUserType()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserResponse> getUserById(Integer id) {
        return userRepository.findById(id)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getUserType()
                ));
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        User user = User.builder()
                .name(userRequest.name())
                .email(userRequest.email())
                .role(userRequest.role())
                .userType(userRequest.userType())
                .build();

        User savedUser = userRepository.save(user);
        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getUserType()
        );
    }

    @Override
    public UserResponse updateUser(Integer id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setRole(userRequest.role());
        user.setUserType(userRequest.userType());

        User updatedUser = userRepository.save(user);
        return new UserResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getUserType()
        );
    }

    @Override
    public void deleteUser(Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    @Override
    public boolean userExists(Integer userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public String getUserType(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getUserType().name(); // Return the userType as a String
        } else {
            log.warn("User with ID {} not found", userId);
            return "UNKNOWN"; // Return "UNKNOWN" if the user doesn't exist
        }
    }

    @Override
    public String getUserRole(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            return user.get().getRole().name(); // Return the userType as a String
        } else {
            log.warn("User with ID {} not found", userId);
            return "UNKNOWN";
        }
    }
}

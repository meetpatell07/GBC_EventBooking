package ca.gbc.userservice.dto;

import ca.gbc.userservice.model.User.Role;
import ca.gbc.userservice.model.User.UserType;

public record UserResponse(
        Integer id,
        String name,
        String email,
        Role role,
        UserType userType
) {

}

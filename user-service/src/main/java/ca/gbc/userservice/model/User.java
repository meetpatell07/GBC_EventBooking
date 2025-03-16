package ca.gbc.userservice.model;

import jakarta.persistence.*;
import lombok.*;



@Entity
@Table(name="t_users")
@Getter // Lombok annotation to generate getter methods for all fields.
@Setter // Lombok annotation to generate setter methods for all fields.
@AllArgsConstructor // Lombok annotation to generate a constructor with all fields as parameters.
@NoArgsConstructor // Lombok annotation to generate a no-argument constructor.
@Builder // Lombok annotation to enable the Builder pattern for creating instances.

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    public enum Role {
        ADMIN, SUPERADMIN, USER
    }

    public enum UserType {
        STUDENT, STAFF, FACULTY
    }
}

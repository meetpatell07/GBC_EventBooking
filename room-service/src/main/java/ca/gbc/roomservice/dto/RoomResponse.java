package ca.gbc.roomservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomResponse {
    private int id;
    private String name;
    private Integer capacity;
    private List<String> features;
    private Boolean availability;
}

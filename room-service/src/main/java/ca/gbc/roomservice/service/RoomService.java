package ca.gbc.roomservice.service;

import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomService {
    RoomResponse createRoom(RoomRequest roomRequest);
    List<RoomResponse> getAllRooms();
    String updateRoom(int id, RoomRequest roomRequest);
    void deleteRoom(int id);
    boolean getRoomAvailability(int id);
    Integer getCapacity(int id);
    boolean roomExists(int id);
}

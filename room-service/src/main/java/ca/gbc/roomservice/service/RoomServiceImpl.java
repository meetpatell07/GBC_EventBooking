package ca.gbc.roomservice.service;


import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.model.Room;
import ca.gbc.roomservice.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;

    @Override
    public RoomResponse createRoom(RoomRequest roomRequest) {
        Room room = Room.builder()
                .name(roomRequest.getName())
                .capacity(roomRequest.getCapacity())
                .features(roomRequest.getFeatures())
                .availability(roomRequest.getAvailability())
                .build();
        Room savedRoom = roomRepository.save(room);
        log.info("Room Saved: {}", roomRequest.getName());

        return RoomResponse.builder()
                .id(savedRoom.getId())
                .name(savedRoom.getName())
                .capacity(savedRoom.getCapacity())
                .features(savedRoom.getFeatures())
                .availability(savedRoom.getAvailability())
                .build();
    }

    @Override
    public List<RoomResponse> getAllRooms() {
        log.info("Fetching all rooms");
        List<Room> rooms = roomRepository.findAll();
        return rooms.stream().map(this::mapToRoomResponse).toList();
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return new RoomResponse(room.getId(), room.getName(),
                room.getCapacity(), room.getFeatures(), room.getAvailability());
    }

    @Override
    public String updateRoom(int id, RoomRequest roomRequest) {
        log.debug("Updating room {}", id);
        Room room = roomRepository.findById(id).orElse(null);
        if (room != null) {
            room.setName(roomRequest.getName());
            room.setCapacity(roomRequest.getCapacity());
            room.setFeatures(roomRequest.getFeatures());
            room.setAvailability(roomRequest.getAvailability());
            roomRepository.save(room);
            log.info("Room with ID {} updated successfully", id);
            return "Room Updated Successfully";
        } else {
            log.warn("Room with ID {} not found", id);
            return "Room Not Found";
        }
    }

    @Override
    public void deleteRoom(int id) {
        log.info("Deleting room with ID: {}", id);
        roomRepository.deleteById(id);
    }

    @Override
    public boolean getRoomAvailability(int id) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            log.info("Room with ID {} not found; returning availability as false", id);
            return false; // Room not found, so we return false for availability
        }
        log.info("Room with ID {} found; availability: {}", id, room.getAvailability());
        return room.getAvailability(); // Return the actual availability status
    }

    @Override
    public Integer getCapacity(int id) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            log.info("Room with ID {} not found; returning capacity as false", id);
            return null;
        }
        log.info("Room with ID {} found; Capacity: {}", id, room.getCapacity());
        return room.getCapacity();
    }

    @Override
    public boolean roomExists(int id) {
        return roomRepository.existsById(id);
    }


}

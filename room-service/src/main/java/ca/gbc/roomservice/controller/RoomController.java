package ca.gbc.roomservice.controller;
import ca.gbc.roomservice.dto.RoomRequest;
import ca.gbc.roomservice.dto.RoomResponse;
import ca.gbc.roomservice.model.Room;
import ca.gbc.roomservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/room")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<RoomResponse> createRoom(@RequestBody RoomRequest roomRequest) {
        RoomResponse createdRoom = roomService.createRoom(roomRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.add("location", "/api/rooms/" + createdRoom.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdRoom);
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable("roomId") Integer roomId,
                                        @RequestBody RoomRequest roomRequest) {
        boolean exists = roomService.roomExists(roomId);
        if(!exists) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Room with ID " + roomId + " does not exist."));
        }
        roomService.updateRoom(roomId, roomRequest);

        String message = "Room with ID " + roomId + " was successfully updated.";

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Collections.singletonMap("message", message));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RoomResponse> getAllRooms() {
        return roomService.getAllRooms();
    }


    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable("roomId") Integer roomId) {
        boolean exists = roomService.roomExists(roomId);
        if(!exists) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Room with ID " + roomId + " does not exist."));
        }
        roomService.deleteRoom(roomId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{roomId}/availability")
    public boolean getRoomAvailability(@PathVariable("roomId") Integer roomId) {
        return roomService.getRoomAvailability(roomId);
    }

    @GetMapping("/{roomId}/capacity")
    public ResponseEntity<Integer> getRoomCapacity(@PathVariable("roomId") Integer roomId) {
        Integer capacity = roomService.getCapacity(roomId);
        return capacity != null ? ResponseEntity.ok(capacity) : ResponseEntity.notFound().build();
    }

    @GetMapping("/{roomId}/exists")
    public ResponseEntity<Boolean> checkRoomExists(@PathVariable("roomId") Integer roomId) throws InterruptedException {
        boolean exists = roomService.roomExists(roomId);
        return exists ? ResponseEntity.ok(true) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

}

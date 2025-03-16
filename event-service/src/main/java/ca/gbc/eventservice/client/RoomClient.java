package ca.gbc.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "room", url = "${room.service.url}")
public interface RoomClient {

    @GetMapping("/api/room/{roomId}/availability")
    boolean isRoomAvailable(@PathVariable("roomId") Integer roomId);
    
    @GetMapping("/api/room/{roomId}/capacity")
    int getRoomCapacity(@PathVariable("roomId") Integer roomId);

    @RequestMapping(method = RequestMethod.GET, value = "/api/room/{roomId}/exists")
    boolean checkRoomExists(@PathVariable("roomId") Integer roomId);

    // Check if the room can accommodate the specified number of attendees
    default boolean isCapacitySufficient(Integer roomId, Integer requiredCapacity) {
        int roomCapacity = getRoomCapacity(roomId);
        return requiredCapacity <= roomCapacity;
    }
}

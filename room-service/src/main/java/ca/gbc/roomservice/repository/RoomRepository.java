package ca.gbc.roomservice.repository;

import ca.gbc.roomservice.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {

}

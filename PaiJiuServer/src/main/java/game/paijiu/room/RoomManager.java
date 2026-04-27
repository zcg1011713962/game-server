package game.paijiu.room;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RoomManager {

    private final Map<Long, Room> roomMap = new ConcurrentHashMap<>();

    public Room getRoom(Long roomId) {
        return roomMap.get(roomId);
    }

    public Room createRoom(Long roomId) {
        Room room = new Room();
        room.setRoomId(roomId);
        roomMap.put(roomId, room);
        return room;
    }
}

package game.paijiu.room;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaiJiuRoomManager {

    private final Map<Long, PaiJiuRoom> roomMap = new ConcurrentHashMap<>();

    public PaiJiuRoom getOrCreate(Long roomId) {
        return roomMap.computeIfAbsent(roomId, id -> new PaiJiuRoom(id, 8));
    }

    public PaiJiuRoom get(Long roomId) {
        return roomMap.get(roomId);
    }

    public void removeIfEmpty(Long roomId) {
        PaiJiuRoom room = roomMap.get(roomId);
        if (room != null && room.getPlayers().isEmpty()) {
            roomMap.remove(roomId);
        }
    }
}

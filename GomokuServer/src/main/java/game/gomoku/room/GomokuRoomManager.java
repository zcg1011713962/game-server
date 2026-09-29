package game.gomoku.room;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import game.gomoku.service.GomokuBotProfileService;
import java.util.Map;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class GomokuRoomManager {
    @Value("${gomoku.bot.enabled:false}")
    private boolean botEnabled;
    @Value("${gomoku.turn-timeout-seconds:30}")
    private long turnTimeoutSeconds;
    private final AtomicLong roomSequence = new AtomicLong(200000);
    private final Map<Long, GomokuRoom> rooms = new ConcurrentHashMap<>();
    private final Map<Long, Long> userRooms = new ConcurrentHashMap<>();
    private final GomokuBotProfileService botProfileService;

    public GomokuRoomManager(GomokuBotProfileService botProfileService) {
        this.botProfileService = botProfileService;
    }

    public synchronized GomokuRoom match(long userId) {
        Long oldRoomId = userRooms.get(userId);
        if (oldRoomId != null && rooms.containsKey(oldRoomId)) return rooms.get(oldRoomId);
        GomokuRoom room = rooms.values().stream().filter(item -> !item.isStarted() && !item.isFinished()).findFirst().orElseGet(() -> {
            GomokuRoom created = new GomokuRoom(roomSequence.incrementAndGet());
            created.setTurnTimeoutSeconds(turnTimeoutSeconds);
            rooms.put(created.getRoomId(), created);
            return created;
        });
        room.join(userId);
        if (botEnabled && !room.isStarted()) {
            room.setBotProfile(botProfileService.selectBot());
            room.join(room.getBotUserId());
        }
        userRooms.put(userId, room.getRoomId());
        return room;
    }

    public GomokuRoom get(long roomId, long userId) {
        GomokuRoom room = rooms.get(roomId);
        if (room == null || (!Long.valueOf(userId).equals(room.getBlackUserId()) && !Long.valueOf(userId).equals(room.getWhiteUserId()))) throw new IllegalStateException("Room not found");
        return room;
    }

    public synchronized void remove(GomokuRoom room) {
        if (room == null) return;
        rooms.remove(room.getRoomId());
        if (room.getBlackUserId() != null) userRooms.remove(room.getBlackUserId(), room.getRoomId());
        if (room.getWhiteUserId() != null) userRooms.remove(room.getWhiteUserId(), room.getRoomId());
    }

    public Collection<GomokuRoom> getRooms() {
        return rooms.values();
    }
}


package game.gomoku.handler;

import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.gomoku.netty.GatewayChannelManager;
import game.gomoku.room.GomokuRoom;
import game.gomoku.room.GomokuRoomManager;
import game.gomoku.util.GameThreadPoolUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GomokuTimeoutService {
    private final GomokuRoomManager roomManager;
    public GomokuTimeoutService(GomokuRoomManager roomManager) {
        this.roomManager = roomManager;
    }

    @Scheduled(fixedDelay = 1000)
    public void checkTimeouts() {
        long now = System.currentTimeMillis();
        for (GomokuRoom room : roomManager.getRooms()) {
            if (!room.isStarted() || room.isFinished()) {
                continue;
            }
            GameThreadPoolUtil.executeRoom(String.valueOf(room.getRoomId()), () -> finishTimeout(room, now));
        }
    }

    private void finishTimeout(GomokuRoom room, long now) {
        if (!room.finishByTimeout(now, room.getTurnTimeoutSeconds() * 1000L)) {
            return;
        }

        Long loserUserId = room.getTurnUserId();
        Long winnerUserId = loserUserId != null && loserUserId.equals(room.getBlackUserId())
                ? room.getWhiteUserId() : room.getBlackUserId();
        Map<String, Object> state = GomokuMatchHandler.state(room);
        state.put("winnerUserId", winnerUserId);
        state.put("loserUserId", loserUserId);
        state.put("finishReason", "timeout");
        GameResponse finish = GameResponse.push(room.getRoomId(), Cmd.GOMOKU_FINISH, state);
        GatewayChannelManager.broadcast(finish);
        roomManager.remove(room);
    }
}

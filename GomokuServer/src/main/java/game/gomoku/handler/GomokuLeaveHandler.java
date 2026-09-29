package game.gomoku.handler;

import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.gomoku.netty.GatewayChannelManager;
import game.gomoku.netty.handler.DispatcherHandler;
import game.gomoku.room.GomokuRoom;
import game.gomoku.room.GomokuRoomManager;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class GomokuLeaveHandler extends DispatcherHandler {
    private final GomokuRoomManager roomManager;
    public GomokuLeaveHandler(GomokuRoomManager roomManager) { super(Cmd.GOMOKU_LEAVE.value()); this.roomManager = roomManager; }
    @Override public void exec(GameRequest req) {
        if (req.getRoomId() == null) return;
        GomokuRoom room = roomManager.get(req.getRoomId(), req.getUserId());
        Map<String, Object> state = GomokuMatchHandler.state(room);
        long winner = Long.valueOf(req.getUserId()).equals(room.getBlackUserId()) ? room.getWhiteUserId() : room.getBlackUserId();
        state.put("winnerUserId", winner);
        state.put("reason", "surrender");
        GameResponse finish = GameResponse.push(room.getRoomId(), Cmd.GOMOKU_FINISH, state);
        finish.setGatewayId(req.getGatewayId());
        GatewayChannelManager.send(req.getGatewayId(), finish);
        roomManager.remove(room);
    }
}

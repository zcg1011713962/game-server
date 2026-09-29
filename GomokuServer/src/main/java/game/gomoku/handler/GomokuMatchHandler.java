package game.gomoku.handler;

import game.common.constant.PushType;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.gomoku.netty.GatewayChannelManager;
import game.gomoku.netty.handler.DispatcherHandler;
import game.gomoku.room.GomokuRoom;
import game.gomoku.room.GomokuRoomManager;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GomokuMatchHandler extends DispatcherHandler {
    private final GomokuRoomManager roomManager;
    public GomokuMatchHandler(GomokuRoomManager roomManager) { super(Cmd.GOMOKU_MATCH.value()); this.roomManager = roomManager; }
    @Override public void exec(GameRequest req) {
        GomokuRoom room = roomManager.match(req.getUserId());
        GameResponse response = GameResponse.ok(req, Cmd.GOMOKU_MATCH_RESULT, state(room));
        response.setRoomId(room.getRoomId());
        GatewayChannelManager.send(req.getGatewayId(), response);
        if (room.isStarted()) {
            GameResponse push = GameResponse.push(room.getRoomId(), Cmd.GOMOKU_START, state(room));
            push.setGatewayId(req.getGatewayId());
            GatewayChannelManager.send(req.getGatewayId(), push);
        }
    }
    static Map<String, Object> state(GomokuRoom room) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("roomId", room.getRoomId()); data.put("blackUserId", room.getBlackUserId()); data.put("whiteUserId", room.getWhiteUserId());
        data.put("turnUserId", room.getTurnUserId()); data.put("started", room.isStarted()); data.put("finished", room.isFinished()); data.put("turnStartedAt", room.getTurnStartedAt()); data.put("turnTimeoutSeconds", room.getTurnTimeoutSeconds()); data.put("botProfile", room.getBotProfile()); data.put("board", room.getBoard());
        return data;
    }
}

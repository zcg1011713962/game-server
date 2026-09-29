package game.gomoku.handler;

import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.gomoku.exception.GameException;
import game.gomoku.netty.GatewayChannelManager;
import game.gomoku.netty.handler.DispatcherHandler;
import game.gomoku.room.GomokuRoom;
import game.gomoku.room.GomokuRoomManager;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class GomokuMoveHandler extends DispatcherHandler {
    private final GomokuRoomManager roomManager;
    private final GomokuBotService botService;

    public GomokuMoveHandler(GomokuRoomManager roomManager, GomokuBotService botService) {
        super(Cmd.GOMOKU_MOVE.value());
        this.roomManager = roomManager;
        this.botService = botService;
    }

    @Override
    public void exec(GameRequest req) {
        if (req.getRoomId() == null || req.getData() == null) {
            throw new GameException(1001, "Room and move coordinates are required");
        }
        Integer x = req.getData().getInteger("x");
        Integer y = req.getData().getInteger("y");
        if (x == null || y == null) {
            throw new GameException(1001, "Move coordinates are required");
        }

        GomokuRoom room = roomManager.get(req.getRoomId(), req.getUserId());
        final int stone;
        try {
            stone = room.move(req.getUserId(), x, y);
        } catch (IllegalArgumentException e) {
            throw new GameException(1001, e.getMessage());
        } catch (IllegalStateException e) {
            throw new GameException(5003, e.getMessage());
        }

        Map<String, Object> state = GomokuMatchHandler.state(room);
        state.put("x", x);
        state.put("y", y);
        state.put("stone", stone);
        GameResponse statePush = GameResponse.push(room.getRoomId(), Cmd.GOMOKU_STATE, state);
        statePush.setGatewayId(req.getGatewayId());
        GatewayChannelManager.send(req.getGatewayId(), statePush);

        if (room.isFinished()) {
            state.put("winnerUserId", stone == 1 ? room.getBlackUserId() : room.getWhiteUserId());
            GameResponse finishPush = GameResponse.push(room.getRoomId(), Cmd.GOMOKU_FINISH, state);
            finishPush.setGatewayId(req.getGatewayId());
            GatewayChannelManager.send(req.getGatewayId(), finishPush);
            roomManager.remove(room);
        }
        else botService.play(room, req.getGatewayId());
    }
}

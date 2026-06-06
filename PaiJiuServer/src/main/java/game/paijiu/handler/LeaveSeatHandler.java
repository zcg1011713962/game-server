package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerLeavePush;
import game.common.entity.res.PlayerLeaveSeatPush;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class LeaveSeatHandler extends DispatcherHandler {

    @Autowired
    private PaiJiuRoomManager roomManager;

    public LeaveSeatHandler() {
        super(Cmd.LEAVE_SEAT.value());
    }

    @Override
    public void exec(GameRequest req) {
        log.info("LeaveSeatHandler:{}", req.getUserId());
        PaiJiuRoom room = roomManager.get(req.getRoomId(), req.getGatewayId());
        if (room == null) {
            return;
        }

        PaiJiuPlayer player = room.leaveSeat(req.getUserId());
        roomManager.save(room);

        PlayerLeaveSeatPush push = PlayerLeaveSeatPush.builder()
                .roomId(room.getRoomId())
                .userId(player.getUserId())
                .seatId(player.getSeatId())
                .reason(1)
                .build();

        GatewayChannelManager.send(
                req.getGatewayId(),
                GameResponse.builder()
                        .traceId(UUID.randomUUID().toString())
                        .gatewayId(req.getGatewayId())
                        .pushType(PushType.ROOM.code())
                        .cmd(Cmd.PLAYER_LEAVE_SEAT)
                        .roomId(room.getRoomId())
                        .userId(req.getUserId())
                        .code(ErrorCode.SUCCESS.code())
                        .data(push)
                        .build()
        );
    }
}

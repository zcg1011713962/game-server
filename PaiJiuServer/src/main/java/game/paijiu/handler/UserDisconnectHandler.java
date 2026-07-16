package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerLeavePush;
import game.common.protocol.Cmd;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class UserDisconnectHandler extends DispatcherHandler {

    @Autowired
    PaiJiuRoomManager roomManager;

    public UserDisconnectHandler() {
        super(Cmd.USER_DISCONNECT.value());
    }

    @Override
    public void exec(GameRequest req) {
        Long roomId = req.getRoomId();
        if (roomId == null) {
            return;
        }

        PaiJiuRoom room = roomManager.getRoom(roomId, req.getGatewayId());
        if (room == null) {
            roomManager.removeUserRoom(req.getUserId(), roomId);
            return;
        }

        PaiJiuPlayer leavePlayer = room.handleDisconnect(req.getUserId());
        roomManager.save(room);

        if (leavePlayer == null) {
            log.info("玩家断线保留房间位置 roomId:{} userId:{}", roomId, req.getUserId());
            return;
        }

        roomManager.removeUserRoom(req.getUserId(), room.getRoomId());
        if (room.getPlayerCount() == 0) {
            roomManager.remove(room.getRoomId());
        }

        PlayerLeavePush push = PlayerLeavePush.builder()
                .player(leavePlayer.toDTO())
                .roomId(room.getRoomId())
                .build();

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_LEAVE)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(push)
                .build());
    }
}

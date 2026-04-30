package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.req.GameRequest;
import game.common.entity.req.NextRoundReq;
import game.common.entity.res.GameResponse;
import game.common.entity.res.NextRoundPush;
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

@Slf4j
@Component
public class NextRoundHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public NextRoundHandler() {
        super(Cmd.NEXT_ROUND.value());
    }

    @Override
    public void exec(GameRequest req) {
        NextRoundReq nextRoundReq = JsonUtil.parse(req.getData().toString(), NextRoundReq.class);

        PaiJiuRoom room = roomManager.get(nextRoundReq.getRoomId());
        if (room == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            log.error("ready room is null");
            return;
        }
        long roundId = room.nextRound();
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(2)
                .cmd(Cmd.NEXT_ROUND_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(NextRoundPush.builder().roundId(roundId).roomId(room.getRoomId()).build())
                .build());
    }
}

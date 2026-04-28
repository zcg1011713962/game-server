package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.Packet;
import game.common.entity.req.EnterRoomReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class EnterRoomHandler extends DispatcherHandler {
    public EnterRoomHandler() {
        super(Cmd.ENTER_ROOM.value());
    }

    @Override
    public void exec(GameRequest gameRequest) {
        log.info("EnterRoomHandler:{}", gameRequest);
        EnterRoomReq enterRoomReq = JsonUtil.parse(gameRequest.getData().toString(), EnterRoomReq.class);
        if(enterRoomReq.getRoomId() == null){
            GatewayChannelManager.send(gameRequest.getGatewayId(), GameResponse.error(gameRequest, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        EnterRoomResp enterRoomResp = EnterRoomResp.builder()
                .roomId(enterRoomReq.getRoomId())
                .userId(gameRequest.getUserId())
                .build();
        GameResponse gameResponse = GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(gameRequest.getGatewayId())
                .pushType(1)
                .cmd(Cmd.ENTER_ROOM_RESULT)
                .userId(gameRequest.getUserId())
                .code(ErrorCode.SUCCESS.code())
                .data(enterRoomResp).build();
        GatewayChannelManager.send(gameRequest.getGatewayId(), gameResponse);
    }
}

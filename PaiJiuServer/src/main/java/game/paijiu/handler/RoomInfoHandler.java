package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.req.EnterRoomReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
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
public class RoomInfoHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public RoomInfoHandler() {
        super(Cmd.ROOM_INFO.value());
    }

    @Override
    public void exec(GameRequest req) {
        EnterRoomReq data = JsonUtil.objToBean(req.getData(), EnterRoomReq.class);
        if(req.getRoomId() == null){
            req.setRoomId(data.getRoomId());
        }
        PaiJiuRoom room = roomManager.get(req.getRoomId());
        if(room == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            log.error("room info is null");
            return;
        }

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(1)
                .cmd(Cmd.ROOM_INFO_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(EnterRoomResp.builder()
                        .roomId(room.getRoomId())
                        .userId(req.getUserId())
                        .roomState(room.getState().code())
                        .players(room.getPlayerDTOList())
                        .build()).build());

    }
}

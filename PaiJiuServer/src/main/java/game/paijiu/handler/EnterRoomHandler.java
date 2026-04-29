package game.paijiu.handler;

import com.alibaba.fastjson2.JSONObject;
import game.common.constant.ErrorCode;
import game.common.entity.PlayerDTO;
import game.common.entity.req.EnterRoomReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerEnterPush;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuPlayer;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import game.paijiu.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class EnterRoomHandler extends DispatcherHandler {
    public EnterRoomHandler() {
        super(Cmd.ENTER_ROOM.value());
    }

    @Autowired
    PaiJiuRoomManager roomManager;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void exec(GameRequest req) {
        log.info("EnterRoomHandler:{}", req);
        EnterRoomReq enterRoomReq = JsonUtil.parse(req.getData().toString(), EnterRoomReq.class);
        if(enterRoomReq.getRoomId() == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        PaiJiuRoom room = roomManager.getOrCreate(req.getRoomId());

        JSONObject jsonObject  = redisUtil.get("player:" + req.getUserId());
        if(jsonObject == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.NOT_LOGIN));
            return;
        }
        PlayerDTO playerDTO = JsonUtil.objToBean(jsonObject, PlayerDTO.class);

        PaiJiuPlayer player = room.enter(playerDTO);
        req.setRoomId(room.getRoomId());
        // 进房成功绑定房间
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(1)
                .cmd(Cmd.ENTER_ROOM_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(EnterRoomResp.builder()
                        .roomId(enterRoomReq.getRoomId())
                        .userId(req.getUserId())
                        .seatId(playerDTO.getSeatId())
                        .roomState(room.getState().code())
                        .players(room.getPlayerDTOList())
                        .build()).build());

        // 广播
        PlayerEnterPush playerEnterPush = PlayerEnterPush.builder().player(player.toDTO()).roomId(room.getRoomId()).build();
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(2)
                .cmd(Cmd.PLAYER_ENTER)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(playerEnterPush).build());


    }
}

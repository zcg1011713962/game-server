package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.req.GameRequest;
import game.common.entity.req.ReadyReq;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
import game.common.entity.res.GameStartPush;
import game.common.entity.res.PlayerReadyPush;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuPlayer;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ReadyHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public ReadyHandler() {
        super(Cmd.READY.value());
    }

    @Override
    public void exec(GameRequest req) {
        ReadyReq data = JsonUtil.objToBean(req.getData(), ReadyReq.class);

        PaiJiuRoom room = roomManager.get(data.getRoomId());
        if (room == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            log.error("ready room is null");
            return;
        }

        req.setRoomId(room.getRoomId());

        PaiJiuPlayer player = room.ready(req.getUserId());

        PlayerReadyPush readyPush = new PlayerReadyPush();
        readyPush.setRoomId(room.getRoomId());
        readyPush.setUserId(req.getUserId());
        readyPush.setSeatId(player.getSeatId());
        readyPush.setState(player.getState().code());
        // 准备返回
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(1)
                .cmd(Cmd.READY_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(readyPush).build());

        // 广播玩家准备
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(2)
                .cmd(Cmd.PLAYER_READY)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(readyPush).build());


        if (room.isAllReady()) {
            log.info("所有玩家准备好，开始游戏");
            room.startGame();
            room.selectBanker();

            GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                    .traceId(UUID.randomUUID().toString())
                    .gatewayId(req.getGatewayId())
                    .pushType(2)
                    .cmd(Cmd.GAME_START)
                    .userId(req.getUserId())
                    .roomId(room.getRoomId())
                    .code(ErrorCode.SUCCESS.code())
                    .data(GameStartPush.builder().bankerSeat(room.getBankerSeat()).roomId(room.getRoomId()).roomState(room.getState().code()).players(room.getPlayerDTOList()).build())
                    .build());
        }
    }
}

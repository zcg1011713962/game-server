package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.req.BetReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerBetPush;
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
public class BetHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public BetHandler() {
        super(Cmd.BET.value());
    }

    @Override
    public void exec(GameRequest req) {
        BetReq data = JsonUtil.objToBean(req.getData(), BetReq.class);

        if (data == null || data.getRoomId() == null || data.getChip() == null) {
            log.error("bet params error");
            return;
        }
        PaiJiuRoom room = roomManager.get(data.getRoomId());
        if (room == null) {
            log.error("bet room is null");
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        req.setRoomId(room.getRoomId());

        int totalBet = room.bet(req.getUserId(), data.getChip());
        Integer seatId = room.getSeatId(req.getUserId());

        PlayerBetPush pushData = new PlayerBetPush();
        pushData.setRoomId(room.getRoomId());
        pushData.setUserId(req.getUserId());
        pushData.setSeatId(seatId);
        pushData.setBetArea(data.getBetArea());
        pushData.setChip(data.getChip());
        pushData.setTotalBet(totalBet);

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(1)
                .cmd(Cmd.BET_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(pushData).build());

        // 广播
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(2)
                .cmd(Cmd.PLAYER_BET)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(pushData).build());
    }
}

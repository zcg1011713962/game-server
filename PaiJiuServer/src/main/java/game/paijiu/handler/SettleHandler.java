package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.req.GameRequest;
import game.common.entity.req.SettleReq;
import game.common.entity.res.GameResponse;
import game.common.entity.res.NextRoundPush;
import game.common.entity.res.SettlePush;
import game.common.protocol.Cmd;
import game.common.util.DelayTaskUtil;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SettleHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public SettleHandler() {
        super(Cmd.SETTLE.value());
    }

    @Override
    public void exec(GameRequest req) {
        // 客户端发牌动画播放完，再结算
        SettleReq data = JsonUtil.objToBean(req.getData(), SettleReq.class);

        PaiJiuRoom room = roomManager.get(data.getRoomId(), req.getGatewayId());
        if (room == null) {
            log.error("bet room is null");
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        long currRoundId = room.getRoundId();
        // 结算
        SettlePush settlePush = room.settle();
        // 房间快照
        roomManager.save(room);

        GatewayChannelManager.send(
                req.getGatewayId(),
                GameResponse.push(room.getRoomId(), Cmd.SETTLE, settlePush)
        );

        DelayTaskUtil.submit(UUID.randomUUID().toString(), ()->{
            long roundId = room.nextRound(currRoundId);
            if(roundId == currRoundId){
                return;
            }
            roomManager.save(room);
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                    .traceId(UUID.randomUUID().toString())
                    .gatewayId(req.getGatewayId())
                    .pushType(PushType.ROOM.code())
                    .cmd(Cmd.NEXT_ROUND_RESULT)
                    .userId(req.getUserId())
                    .roomId(room.getRoomId())
                    .code(ErrorCode.SUCCESS.code())
                    .data(NextRoundPush.builder()
                            .roundId(roundId)
                            .roomState(room.getState().code())
                            .roomId(room.getRoomId())
                            .players(room.getPlayerDTOList())
                            .build())
                    .build());
        }, 5, TimeUnit.SECONDS);
    }
}

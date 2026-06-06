package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.req.EnterRoomReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerEnterPush;
import game.common.protocol.Cmd;
import game.common.util.CommonUtil;
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
        Long oldRoomId = roomManager.getRoomIdByUserId(req.getUserId());
        log.info("RoomInfoHandler {} oldRoomId:{}", req.getUserId(), oldRoomId);
        if (oldRoomId != null) {
            PaiJiuRoom room = roomManager.get(oldRoomId, req.getGatewayId());
            if(room != null){
                // 进房成功
                GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                        .traceId(UUID.randomUUID().toString())
                        .gatewayId(req.getGatewayId())
                        .pushType(PushType.SINGLE.code())
                        .cmd(Cmd.ENTER_ROOM_RESULT)
                        .userId(req.getUserId())
                        .roomId(room.getRoomId())
                        .code(ErrorCode.SUCCESS.code())
                        .data(EnterRoomResp.builder()
                                .roomId(room.getRoomId())
                                .userId(req.getUserId())
                                .roundId(room.getRoundId())
                                .roomState(room.getState().code())
                                .players(room.getPlayerDTOList())
                                .seats(CommonUtil.toStringKeyMap(room.getSeats()))
                                .betMap(CommonUtil.toStringKeyMap(room.getBetMap()))
                                .cardMap(CommonUtil.toStringKeyMap(room.getCardMap()))
                                .settlePush(room.getSettlePush())
                                .bankerSeat(room.getBankerSeat())
                                .baseScore(room.getBaseScore())
                                .build()).build());
            }
        }
    }
}

package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerOpenCardPush;
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
public class OpenCardHandler extends DispatcherHandler {
    @Autowired
    private PaiJiuRoomManager roomManager;

    public OpenCardHandler() {
        super(Cmd.OPEN_CARD.value());
    }

    @Override
    public void exec(GameRequest req) {
        Long roomId = req.getRoomId();
        Integer openType = 0;

        if (req.getData() != null) {
            if (roomId == null) {
                roomId = req.getData().getLong("roomId");
            }
            Integer dataOpenType = req.getData().getInteger("openType");
            if (dataOpenType != null) {
                openType = dataOpenType;
            }
        }

        if (roomId == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.PARAM_ERROR));
            return;
        }

        PaiJiuRoom room = roomManager.get(roomId, req.getGatewayId());
        if (room == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        req.setRoomId(room.getRoomId());

        PaiJiuPlayer player = room.openCard(req.getUserId());
        if (player == null) {
            return;
        }

        roomManager.save(room);

        PlayerOpenCardPush push = PlayerOpenCardPush.builder()
                .roomId(room.getRoomId())
                .roundId(room.getRoundId())
                .userId(req.getUserId())
                .seatId(player.getSeatId())
                .openType(openType)
                .roomState(room.getState().code())
                .serverTime(System.currentTimeMillis())
                .build();

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_OPEN_CARD)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(push)
                .build());

        // 亮牌只记录状态并广播，结算统一由 BetHandler 下发的 settleTime 触发。
        // 这样最后一局和断线重连都严格按同一条时间轴走，避免提前翻牌结算。
    }
}


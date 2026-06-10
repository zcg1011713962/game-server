package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.constant.RoomState;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.req.GameRequest;
import game.common.entity.req.GrabBankerReq;
import game.common.entity.res.GameResponse;
import game.common.entity.res.GrabBankerPush;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
@Slf4j
public class GrabBankerHandler extends DispatcherHandler {

    @Autowired
    private PaiJiuRoomManager roomManager;

    public GrabBankerHandler() {
        super(Cmd.GRAB_BANKER.value());
    }

    @Override
    public void exec(GameRequest req) {
        GrabBankerReq data = JsonUtil.objToBean(req.getData(), GrabBankerReq.class);
        log.info("GrabBankerHandler userId={}, data={}", req.getUserId(), JsonUtil.toJson(data));

        if (data == null || data.getRoomId() == null) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.PARAM_ERROR));
            return;
        }

        Integer grabBanker = data.getGrabBanker();
        if (!Objects.equals(grabBanker, 0) && !Objects.equals(grabBanker, 1)) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.PARAM_ERROR));
            return;
        }

        PaiJiuRoom room = roomManager.get(data.getRoomId(), req.getGatewayId());
        if (room == null) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        req.setRoomId(room.getRoomId());

        if (room.getState() != RoomState.GRAB_BANKER) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.GRAB_BANKER_ERROR));
            return;
        }

        PaiJiuPlayer player = room.getPlayer(req.getUserId());
        if (player == null) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.NOT_IN_ROOM));
            return;
        }

        if (player.getSeatId() == null || player.getSeatId() < 0) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.NOT_IN_ROOM));
            return;
        }

        if (player.getGrabBanker() != null) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.GRAB_BANKER_ERROR));
            return;
        }

        player.setGrabBanker(grabBanker);

        GrabBankerPush push = GrabBankerPush.builder()
                .roomId(room.getRoomId())
                .userId(req.getUserId())
                .seatId(player.getSeatId())
                .grabBanker(grabBanker)
                .roomState(room.getState().code())
                .serverTime(System.currentTimeMillis())
                .build();

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_GRAB_BANKER)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(push)
                .build());

        if (room.isAllGrabBankerDone()) {
            room.finishGrabBanker(req.getGatewayId());
        } else {
            roomManager.save(room);
        }
    }
}
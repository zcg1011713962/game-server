package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.User;
import game.common.entity.req.GameRequest;
import game.common.entity.req.SitDownReq;
import game.common.entity.res.GameResponse;
import game.common.entity.res.SitDownResp;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import game.paijiu.service.GameUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class SitDownHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;
    @Autowired
    GameUserService userService;

    public SitDownHandler() {
        super(Cmd.SIT_DOWN.value());
    }

    @Override
    public void exec(GameRequest req) {
        SitDownReq data = JsonUtil.objToBean(req.getData(), SitDownReq.class);

        if (data == null || data.getRoomId() == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.PARAM_ERROR));
            return;
        }
        User user = userService.getUserById(req.getUserId());
        if(user.getGold() <= 0) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.GOLD_NOT_ENOUGH));
            return;
        }
        PaiJiuRoom room = roomManager.get(data.getRoomId());
        if (room == null) {
            GatewayChannelManager.send(req.getGatewayId(),
                    GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        PaiJiuPlayer player = room.sitDown(req.getUserId(), data.getSeatId());
        // 房间快照
        roomManager.save(room);

        SitDownResp resp = SitDownResp.builder()
                .roomId(room.getRoomId())
                .userId(req.getUserId())
                .seatId(player.getSeatId())
                .state(player.getState().code())
                .build();

        // 回自己
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(req.getTraceId())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.SINGLE.code())
                .cmd(Cmd.SIT_DOWN_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .seq(req.getSeq())
                .code(ErrorCode.SUCCESS.code())
                .msg(ErrorCode.SUCCESS.msg())
                .data(resp)
                .build());

        // 广播全房间
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_SIT_DOWN)
                .roomId(room.getRoomId())
                .seq(0)
                .code(ErrorCode.SUCCESS.code())
                .msg(ErrorCode.SUCCESS.msg())
                .data(resp)
                .build());
    }
}

package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.constant.RedisKeyConstants;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.User;
import game.common.entity.req.EnterRoomReq;
import game.common.entity.req.GameRequest;
import game.common.entity.req.LeaveRoomReq;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerEnterPush;
import game.common.entity.res.PlayerLeavePush;
import game.common.protocol.Cmd;
import game.common.util.CommonUtil;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import game.paijiu.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class LeaveRoomHandler extends DispatcherHandler {

    @Autowired
    PaiJiuRoomManager roomManager;
    @Autowired
    RedisUtil redisUtil;

    public LeaveRoomHandler() {
        super(Cmd.LEAVE_ROOM.value());
    }

    @Override
    public void exec(GameRequest req) {
        log.info("LeaveRoomHandler:{}", req);
        LeaveRoomReq leaveRoomReq = JsonUtil.parse(req.getData().toString(), LeaveRoomReq.class);
        if(leaveRoomReq.getRoomId() == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        PaiJiuRoom room = roomManager.getRoom(req.getRoomId());
        if(room == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        User user = redisUtil.get(RedisKeyConstants.player(req.getUserId()), User.class);
        if(user == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.NOT_LOGIN));
            return;
        }
        // 离开房间
        PaiJiuPlayer paiJiuPlayer = room.leave(user.getId());
        roomManager.removeUserRoom(user.getId(), room.getRoomId());
        // 房间快照
        roomManager.save(room);
        if(room.getPlayerCount() == 0){
            // 解散房间
            roomManager.remove(room.getRoomId());
        }


        if(paiJiuPlayer == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.NOT_IN_ROOM));
            return;
        }

        // 离开房间
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.SINGLE.code())
                .cmd(Cmd.LEAVE_ROOM_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(null).build());

        // 广播
        PlayerLeavePush playerLeavePush = PlayerLeavePush.builder().player(paiJiuPlayer.toDTO()).roomId(room.getRoomId()).build();
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_LEAVE)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(playerLeavePush).build());
    }


}

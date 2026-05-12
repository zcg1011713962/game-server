package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.constant.RedisKeyConstants;
import game.common.constant.RoomType;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.User;
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
import game.paijiu.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 自由匹配
 */
@Slf4j
@Component
public class FreeMatchHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;
    @Autowired
    RedisUtil redisUtil;


    public FreeMatchHandler() {
        super(Cmd.FREE_MATCH.value());
    }

    @Override
    public void exec(GameRequest req) {
        log.info("FreeMatchHandler:{}", req);
        User user = redisUtil.get(RedisKeyConstants.player(req.getUserId()), User.class);
        if(user == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.NOT_LOGIN));
            return;
        }
        Long oldRoomId= roomManager.getRoomIdByUserId(req.getUserId());

        PaiJiuRoom room = null;
        if (oldRoomId != null) {
            room = roomManager.get(oldRoomId);
        }else{
            room = roomManager.findWaitRoom();
            if(room == null){ // 没有空房间
                room = roomManager.createRoom(RoomType.FREE_MATCH);
            }
        }
        PaiJiuPlayer paiJiuPlayer = room.enter(user);
        roomManager.saveUserRoom(user.getId(), room.getRoomId());
        roomManager.save(room);
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
                        .baseScore(room.getBaseScore())
                        .seats(CommonUtil.toStringKeyMap(room.getSeats()))
                        .betMap(CommonUtil.toStringKeyMap(room.getBetMap()))
                        .cardMap(CommonUtil.toStringKeyMap(room.getCardMap()))
                        .settlePush(room.getSettlePush())
                        .build()).build());

        // 广播
        PlayerEnterPush playerEnterPush = PlayerEnterPush.builder().player(paiJiuPlayer.toDTO()).roomId(room.getRoomId()).build();
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_ENTER)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(playerEnterPush).build());
    }


}

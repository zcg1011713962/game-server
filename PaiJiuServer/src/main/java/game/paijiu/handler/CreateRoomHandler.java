package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.constant.RedisKeyConstants;
import game.common.constant.RoomType;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.User;
import game.common.entity.req.GameRequest;
import game.common.entity.res.EnterRoomResp;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerEnterPush;
import game.common.protocol.Cmd;
import game.common.util.CommonUtil;
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
public class CreateRoomHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;
    @Autowired
    RedisUtil redisUtil;

    public CreateRoomHandler() {
        super(Cmd.CREATE_ROOM.value());
    }

    @Override
    public void exec(GameRequest req) {
        Long oldRoomId= roomManager.getRoomIdByUserId(req.getUserId());
        if (oldRoomId != null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.EXIST_IN_OTHER_ROOM));
            return;
        }
        PaiJiuRoom room = roomManager.createRoom(RoomType.LOCK_MATCH);
        User user = redisUtil.get(RedisKeyConstants.player(req.getUserId()), User.class);
        if(user == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.NOT_LOGIN));
            return;
        }
        // 进房
        PaiJiuPlayer paiJiuPlayer = room.enter(user);
        // 记录玩家进房
        roomManager.saveUserRoom(user.getId(), room.getRoomId());
        // 房间快照
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
                        .seats(CommonUtil.toStringKeyMap(room.getSeats()))
                        .betMap(CommonUtil.toStringKeyMap(room.getBetMap()))
                        .cardMap(CommonUtil.toStringKeyMap(room.getCardMap()))
                        .settlePush(room.getSettlePush())
                        .build()).build());

        // 房间广播
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

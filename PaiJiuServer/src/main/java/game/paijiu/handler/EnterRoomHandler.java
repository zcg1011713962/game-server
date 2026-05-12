package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
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
import game.common.entity.PaiJiuPlayer;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import game.paijiu.service.GameUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class EnterRoomHandler extends DispatcherHandler {
    public EnterRoomHandler() {
        super(Cmd.ENTER_ROOM.value());
    }

    @Autowired
    PaiJiuRoomManager roomManager;
    @Autowired
    GameUserService userService;

    @Override
    public void exec(GameRequest req) {
        log.info("EnterRoomHandler:{}", req);
        EnterRoomReq enterRoomReq = JsonUtil.parse(req.getData().toString(), EnterRoomReq.class);
        if(enterRoomReq.getRoomId() == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        PaiJiuRoom room = roomManager.getRoom(req.getRoomId());
        if(room == null){
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        User user = userService.getUserById(req.getUserId());
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

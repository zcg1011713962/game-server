package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.constant.RoomState;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.req.GameRequest;
import game.common.entity.req.ReadyReq;
import game.common.entity.res.GameResponse;
import game.common.entity.res.GameStartPush;
import game.common.entity.res.PlayerReadyPush;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import game.paijiu.util.DelayTaskUtil;
import game.paijiu.util.TimerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ReadyHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public ReadyHandler() {
        super(Cmd.READY.value());
    }

    @Override
    public void exec(GameRequest req) {
        ReadyReq data = JsonUtil.objToBean(req.getData(), ReadyReq.class);
        log.info("ReadyHandler:{} {}", req.getUserId(), JsonUtil.toJson(data));

        PaiJiuRoom room = roomManager.get(data.getRoomId(), req.getGatewayId());
        if (room == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            log.error("ready room is null");
            return;
        }

        req.setRoomId(room.getRoomId());

        PaiJiuPlayer player = room.ready(req.getUserId());

        PlayerReadyPush readyPush = PlayerReadyPush.builder()
                .roomId(room.getRoomId())
                .userId(req.getUserId())
                .seatId(player.getSeatId())
                .state(player.getState().code())
                .roomStatus(room.getState().code())
                .build();
        // 准备返回
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.SINGLE.code())
                .cmd(Cmd.READY_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(readyPush).build());

        // 广播玩家准备
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_READY)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(readyPush).build());


        if (room.getAllReady()) {
            room.startGame();
            // 推送游戏开始
            long now = System.currentTimeMillis();
            long roundAnimStartTime = TimerUtil.getRoundAnimStartTime(now);
            long roundAnimEndTime = TimerUtil.getRoundAnimEndTime(now);
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                    .traceId(UUID.randomUUID().toString())
                    .gatewayId(req.getGatewayId())
                    .pushType(PushType.ROOM.code())
                    .cmd(Cmd.GAME_START)
                    .userId(req.getUserId())
                    .roomId(room.getRoomId())
                    .code(ErrorCode.SUCCESS.code())
                    .data(GameStartPush.builder()
                            .roomId(room.getRoomId())
                            .roundId(room.getRoundId())
                            .players(room.getPlayerDTOList())
                            .serverTime(now)
                            .roundAnimStartTime(roundAnimStartTime)
                            .roundAnimEndTime(roundAnimEndTime)
                            .build())
                    .build());
            // 开始游戏动画播放完
            long delay = Math.max(0, roundAnimEndTime - System.currentTimeMillis());
            DelayTaskUtil.getInstance().scheduleMillis(()->{
                try {
                    // 进入抢庄阶段
                    room.startGrabBanker(req.getGatewayId());
                } catch (Exception e) {
                    log.error("进入抢庄阶段异常 roomId={}", room.getRoomId(), e);
                }
            }, delay);
        }
        // 房间快照
        roomManager.save(room);
    }
}

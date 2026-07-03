package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.constant.RoomState;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.entity.res.NextRoundPush;
import game.common.entity.res.PlayerOpenCardPush;
import game.common.entity.res.SettlePush;
import game.common.protocol.Cmd;
import game.common.util.DelayTaskUtil;
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

    private static final long EARLY_NEXT_ROUND_DELAY_MS = 1200L;

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

        if (room.isAllOpenCardDone()) {
            long now = System.currentTimeMillis();
            long nextRoundTime = room.getCurrentNextRoundTime() > 0 ? room.getCurrentNextRoundTime() : now;
            SettlePush settlePush = room.settle(now, now, nextRoundTime);
            roomManager.save(room);

            GatewayChannelManager.send(
                    req.getGatewayId(),
                    GameResponse.push(
                            room.getRoomId(),
                            Cmd.SETTLE,
                            settlePush
                    )
            );

            scheduleEarlyNextRound(req, room.getRoomId());
        }
    }

    private void scheduleEarlyNextRound(GameRequest req, Long roomId) {
        DelayTaskUtil.getInstance().scheduleMillis(() -> {
            try {
                PaiJiuRoom currRoom = roomManager.get(roomId, req.getGatewayId());

                if (currRoom == null) {
                    log.warn("提前进入下一轮失败，房间不存在 roomId={}", roomId);
                    return;
                }

                if (currRoom.getState() != RoomState.SETTLE) {
                    log.warn("提前进入下一轮跳过，房间状态不是SETTLE roomId={} state={}", currRoom.getRoomId(), currRoom.getState());
                    return;
                }

                currRoom.nextRound();
                roomManager.save(currRoom);

                long now = System.currentTimeMillis();
                NextRoundPush nextRoundPush = NextRoundPush.builder()
                        .roomId(currRoom.getRoomId())
                        .roundId(currRoom.getRoundId())
                        .roomState(currRoom.getState().code())
                        .players(currRoom.getPlayerDTOList())
                        .serverTime(now)
                        .nextRoundTime(now)
                        .build();

                GatewayChannelManager.send(
                        req.getGatewayId(),
                        GameResponse.push(
                                currRoom.getRoomId(),
                                Cmd.NEXT_ROUND,
                                nextRoundPush
                        )
                );
            } catch (Exception e) {
                log.error("提前进入下一轮异常 roomId={}", roomId, e);
            }
        }, EARLY_NEXT_ROUND_DELAY_MS);
    }
}

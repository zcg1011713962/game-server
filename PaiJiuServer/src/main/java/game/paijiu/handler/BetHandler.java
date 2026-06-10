package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PlayerState;
import game.common.constant.PushType;
import game.common.constant.RoomState;
import game.common.entity.CardInfo;
import game.common.entity.PaiJiuPlayer;
import game.common.entity.PlayerCardDTO;
import game.common.entity.User;
import game.common.entity.req.BetReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.DealCardPush;
import game.common.entity.res.GameResponse;
import game.common.entity.res.NextRoundPush;
import game.common.entity.res.PlayerBetPush;
import game.common.entity.res.SettlePush;
import game.common.protocol.Cmd;
import game.common.service.RedisUserService;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import game.paijiu.service.GamePushService;
import game.paijiu.util.CardUtils;
import game.paijiu.util.DelayTaskUtil;
import game.paijiu.util.TimerUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class BetHandler extends DispatcherHandler {

    @Autowired
    private PaiJiuRoomManager roomManager;

    @Autowired
    private GamePushService gamePushService;

    @Autowired
    private RedisUserService redisUserService;

    public BetHandler() {
        super(Cmd.BET.value());
    }

    @Override
    public void exec(GameRequest req) {

        BetReq data = JsonUtil.objToBean(req.getData(), BetReq.class);

        if (data == null || data.getRoomId() == null || data.getChip() == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.PARAM_ERROR));
            return;
        }

        log.info("BetHandler userId={} data={}", req.getUserId(), JsonUtil.toJson(data));

        User user = redisUserService.getUserById(req.getUserId());

        if (user == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.USER_NOT_FOUND_ERROR));
            return;
        }

        PaiJiuRoom room = roomManager.get(data.getRoomId(), req.getGatewayId());

        if (room == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        if (room.getState() != RoomState.BET) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.NOT_BET_STATUS));
            return;
        }

        long totalBet = room.bet(req.getUserId(), data.getChip());

        Integer seatId = room.getSeatId(req.getUserId());

        PlayerBetPush pushData = PlayerBetPush.builder()
                .roomId(room.getRoomId())
                .userId(req.getUserId())
                .seatId(seatId)
                .betArea(data.getBetArea())
                .chip(data.getChip())
                .totalBet(totalBet)
                .players(room.getPlayerDTOList())
                .build();

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.SINGLE.code())
                .cmd(Cmd.BET_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(pushData)
                .build());

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.ROOM.code())
                .cmd(Cmd.PLAYER_BET)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(pushData)
                .build());

        if (!room.canDeal()) {
            roomManager.save(room);
            return;
        }

        startDeal(req, room);
    }

    /**
     * 所有人下注完成，进入发牌阶段
     */
    private void startDeal(GameRequest req, PaiJiuRoom room) {
        if (room.getState() == RoomState.DEAL) {
            return;
        }
        room.setState(RoomState.DEAL);

        List<List<CardInfo>> hands = CardUtils.deal(room.getPlayingCount());
        List<PlayerCardDTO> playerCardList = new ArrayList<>();
        int index = 0;
        for (PaiJiuPlayer p : room.getPlayers().values()) {
            if (p.getState() != PlayerState.PLAYING) {
                continue;
            }
            List<CardInfo> hand = hands.get(index++);
            room.getCardMap().put(p.getUserId(), hand);
            PlayerCardDTO dto = new PlayerCardDTO();
            dto.setUserId(p.getUserId());
            dto.setSeatId(p.getSeatId());
            dto.setCards(hand);
            playerCardList.add(dto);
        }
        roomManager.save(room);
        /**
         * 时间轴：
         * +1000ms   开始发牌
         * +6000ms   翻牌
         * +9000ms   结算
         * +13000ms  下一轮
         */
        long now = System.currentTimeMillis();
        long dealStartTime = TimerUtil.getDealStartTime(now);
        long showCardTime = TimerUtil.getShowCardStartTime(now);
        long settleStartTime = TimerUtil.getSettleStartTime(now);
        long nextRoundStartTime = TimerUtil.getNextRoundStartTime(now);

        DealCardPush dealCardPush = DealCardPush.builder()
                .roomId(room.getRoomId())
                .roomState(room.getState().code())
                .bankerSeat(room.getBankerSeat())
                .playerCards(playerCardList)
                .serverTime(now)
                .dealStartTime(dealStartTime)
                .showCardTime(showCardTime)
                .settleTime(settleStartTime)
                .nextRoundTime(nextRoundStartTime)
                .build();

        GatewayChannelManager.send(
                req.getGatewayId(),
                GameResponse.push(
                        room.getRoomId(),
                        Cmd.DEAL_CARD,
                        dealCardPush
                )
        );

        scheduleSettle(req, room.getRoomId(), settleStartTime, nextRoundStartTime);
        scheduleNextRound(req, room.getRoomId(), nextRoundStartTime);
    }

    /**
     * 定时结算
     */
    private void scheduleSettle(
            GameRequest req,
            Long roomId,
            long settleStartTime,
            long nextRoundStartTime
    ) {

        long settleDelayMs = Math.max(0L, settleStartTime - System.currentTimeMillis());

        DelayTaskUtil.getInstance().scheduleMillis(() -> {

            try {
                PaiJiuRoom currRoom = roomManager.get(roomId, req.getGatewayId());

                if (currRoom == null) {
                    log.warn("结算失败，房间不存在 roomId={}", roomId);
                    return;
                }

                if (currRoom.getState() != RoomState.DEAL) {
                    log.warn("结算跳过，房间状态不是DEAL roomId={} state={}", currRoom.getRoomId(), currRoom.getState());
                    return;
                }

                SettlePush settlePush = currRoom.settle(System.currentTimeMillis(), settleStartTime, nextRoundStartTime);
                roomManager.save(currRoom);

                GatewayChannelManager.send(
                        req.getGatewayId(),
                        GameResponse.push(
                                currRoom.getRoomId(),
                                Cmd.SETTLE,
                                settlePush
                        )
                );

            } catch (Exception e) {
                log.error("结算异常 roomId={}", roomId, e);
            }

        }, settleDelayMs);
    }

    /**
     * 定时自动进入下一轮
     */
    private void scheduleNextRound(
            GameRequest req,
            Long roomId,
            long nextRoundStartTime
    ) {

        long nextRoundDelayMs = Math.max(0L, nextRoundStartTime - System.currentTimeMillis());

        DelayTaskUtil.getInstance().scheduleMillis(() -> {

            try {
                PaiJiuRoom currRoom = roomManager.get(roomId, req.getGatewayId());

                if (currRoom == null) {
                    log.warn("下一轮失败，房间不存在 roomId={}", roomId);
                    return;
                }

                if (currRoom.getState() != RoomState.SETTLE) {
                    log.warn("下一轮跳过，房间状态不是SETTLE roomId={} state={}", currRoom.getRoomId(), currRoom.getState());
                    return;
                }

                /**
                 * 这里需要你在 PaiJiuRoom 里实现 nextRound()
                 * 建议里面做：
                 * 1. roundId + 1
                 * 2. state = READY 或 WAIT
                 * 3. 清空 betMap
                 * 4. 清空 cardMap
                 * 5. 清空结算数据
                 * 6. 玩家状态改成 SIT/READY前状态
                 */
                currRoom.nextRound();

                roomManager.save(currRoom);

                NextRoundPush nextRoundPush = NextRoundPush.builder()
                        .roomId(currRoom.getRoomId())
                        .roundId(currRoom.getRoundId())
                        .roomState(currRoom.getState().code())
                        .players(currRoom.getPlayerDTOList())
                        .serverTime(System.currentTimeMillis())
                        .nextRoundTime(nextRoundStartTime)
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
                log.error("自动进入下一轮异常 roomId={}", roomId, e);
            }

        }, nextRoundDelayMs);
    }
}
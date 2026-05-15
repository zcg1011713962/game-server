package game.paijiu.room;


import com.alibaba.fastjson2.JSONObject;
import game.common.constant.*;
import game.common.entity.*;
import game.common.entity.req.BetReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.SettlePush;
import game.paijiu.netty.handler.Handler;
import game.paijiu.util.CardUtils;
import game.paijiu.util.DelayTaskUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class PaiJiuRoom {
    // 当前局号
    private long roundId = 1;

    private Long roomId;

    private int maxSeat = 8;
    // 房主
    private Long ownerUserId;
    // 庄家
    private Integer bankerSeat = -1;
    // 房间状态
    private RoomState state = RoomState.WAIT;
    // 房间类型
    private RoomType roomType = RoomType.FREE_MATCH;

    private long baseScore = 0;

    private long betEndTime = 0;
    private int betSeconds = 15;
    private ScheduledFuture<?> scheduledFuture;


    /**
     * userId -> 玩家
     */
    private Map<Long, PaiJiuPlayer> players = new ConcurrentHashMap<>();

    /**
     * seatId -> userId
     */
    private Map<Integer, Long> seats = new ConcurrentHashMap<>();

    /**
     * userId -> 总下注
     */
    private Map<Long, Long> betMap = new ConcurrentHashMap<>();
    /**
     * userId -> 手牌
     */
    private Map<Long, List<CardInfo>> cardMap = new ConcurrentHashMap<>();

    // 结算结果
    private SettlePush settlePush;

    public RoomDTO toRoomDTO(){
        return RoomDTO.builder()
                .roundId(roundId)
                .roomId(roomId)
                .maxSeat(maxSeat)
                .ownerUserId(ownerUserId)
                .bankerSeat(bankerSeat)
                .baseScore(baseScore)
                .state(state)
                .players(players)
                .seats(seats)
                .betMap(betMap)
                .settlePush(settlePush)
                .cardMap(cardMap).build();
    }

    public PaiJiuRoom(Long roomId, RoomType roomType, int maxSeat, long baseScore) {
        this.roomId = roomId;
        this.maxSeat = maxSeat;
        this.roomType = roomType;
        this.baseScore = baseScore;
    }

    public synchronized PaiJiuPlayer enter(User info) {
        PaiJiuPlayer old = players.get(info.getId());
        if (old != null) {
            old.setOnline(true);
            return old;
        }

        if (players.isEmpty()) {
            ownerUserId = info.getId();
        }

        PaiJiuPlayer player = new PaiJiuPlayer();
        player.setUserId(info.getId());
        player.setState(PlayerState.NONE);
        player.setOnline(true);
        player.setNickname(info.getNickname());
        player.setGold(info.getGold());
        player.setAvatar(info.getAvatar());

        players.put(info.getId(), player);
        return player;
    }

    public synchronized PaiJiuPlayer sitDown(Long userId, Integer seatId) {
        PaiJiuPlayer player = players.get(userId);
        if (player == null) {
            throw new RuntimeException("玩家不在房间");
        }

        if(player.getState().code() > PlayerState.SIT.code()){
            throw new RuntimeException("用户状态不允许坐下" + player.getState());
        }

        Integer finalSeatId = seatId;

        if (finalSeatId == null) {
            finalSeatId = findEmptySeat();
        }

        if (finalSeatId == null) {
            throw new RuntimeException("没有空座位");
        }

        if (finalSeatId < 0 || finalSeatId >= maxSeat) {
            throw new RuntimeException("座位非法");
        }

        if (seats.containsKey(finalSeatId)) {
            throw new RuntimeException("座位已被占用");
        }

        if (this.state.code() > RoomState.READY.code()) {
            throw new RuntimeException("房间状态不允许坐下");
        }

        seats.entrySet().removeIf(entry -> entry.getValue().equals(userId));
        player.setSeatId(finalSeatId);
        player.setState(PlayerState.SIT);

        seats.put(finalSeatId, userId);
        return player;
    }

    public synchronized PaiJiuPlayer leave(Long userId) {
        PaiJiuPlayer player = players.remove(userId);
        if (player == null) {
            return null;
        }

        if (player.getSeatId() != null && player.getSeatId() >= 0) {
            seats.remove(player.getSeatId());
        }

        betMap.remove(userId);

        if (Objects.equals(ownerUserId, userId)) {
            ownerUserId = players.keySet().stream().findFirst().orElse(null);
        }

        if (players.isEmpty()) {
            state = RoomState.WAIT;
        }
        return player;
    }

    public synchronized PaiJiuPlayer ready(Long userId) {
        PaiJiuPlayer player = players.get(userId);
        if (player == null) {
            throw new RuntimeException("玩家不在房间");
        }

        if (player.getSeatId() == null || player.getSeatId() < 0) {
            throw new RuntimeException("玩家未入座");
        }

        if (state != RoomState.WAIT && state != RoomState.READY) {
            try{
                TimeUnit.MILLISECONDS.sleep(500);
            }catch (InterruptedException e){
                log.error("ready:{}", e.getMessage());
            }
            if(state != RoomState.WAIT && state != RoomState.READY){
                throw new RuntimeException("当前状态不能准备");
            }
        }

        player.setState(PlayerState.READY);
        state = RoomState.READY;
        return player;
    }

    public synchronized PaiJiuPlayer cancelReady(Long userId) {
        PaiJiuPlayer player = players.get(userId);
        if (player == null) {
            throw new RuntimeException("玩家不在房间");
        }

        if (player.getSeatId() == null || player.getSeatId() < 0) {
            throw new RuntimeException("玩家未入座");
        }

        if (state != RoomState.READY) {
            throw new RuntimeException("当前状态不能取消准备");
        }

        player.setState(PlayerState.SIT);
        boolean readyFlag = false;
        for(PaiJiuPlayer p : players.values()){
            if(p.getState() == PlayerState.READY){
                readyFlag = true;
            }
        }
        state = readyFlag ? RoomState.READY : RoomState.WAIT;
        return player;
    }

    public synchronized boolean getAllReady() {
        if (players.isEmpty()) {
            return false;
        }

        List<PaiJiuPlayer> seated = players.values()
                .stream()
                .filter(p -> p.getSeatId() != null && p.getSeatId() >= 0)
                .toList();

        if (seated.isEmpty()) {
            return false;
        }

        boolean flag = seated.stream().allMatch(p -> p.getState() == PlayerState.READY);
        if(seated.size() > 1){
            return flag;
        }
        return false;
    }

    public synchronized void startGame() {
        state = RoomState.BET;

        for (PaiJiuPlayer player : players.values()) {
            if (player.getSeatId() != null && player.getSeatId() >= 0) {
                player.setState(PlayerState.PLAYING);
            }
        }

        betMap.clear();
    }

    public synchronized long bet(Long userId, long chip) {
        if (state != RoomState.BET) {
            throw new RuntimeException("当前不是下注阶段");
        }

        PaiJiuPlayer player = players.get(userId);
        if (player == null) {
            throw new RuntimeException("玩家不在房间");
        }

        if (player.getState() != PlayerState.PLAYING) {
            throw new RuntimeException("玩家不在游戏中");
        }

        if (chip <= 0) {
            throw new RuntimeException("下注金额错误");
        }

        if(player.getGold() < chip){
            throw new RuntimeException("金币不足");
        }
        player.reduceGold(chip);
        return betMap.merge(userId, chip, Long::sum);
    }

    public synchronized boolean canDeal() {
        long playingCount = getPlayingCount();
        long betCount = betMap.keySet().stream()
                .filter(players::containsKey)
                .count();

        return playingCount > 0 && betCount >= playingCount - 1;
    }


    public synchronized SettlePush settle() {

        if (state != RoomState.DEAL) {
            throw new RuntimeException("当前不是结算阶段");
        }

        Long bankerUserId = seats.get(bankerSeat);
        if (bankerUserId == null) {
            throw new RuntimeException("庄家不存在");
        }

        List<CardInfo> bankerCards = cardMap.get(bankerUserId);
        if (bankerCards == null) {
            throw new RuntimeException("庄家牌不存在");
        }

        List<SettlePlayerDTO> result = new ArrayList<>();
        long playerWin = 0;
        long bankerBet = 0;
        // 结算闲家
        for (PaiJiuPlayer player : players.values()) {
            if(player.getUserId().equals(bankerUserId)){
                continue;
            }
            if (player.getState() != PlayerState.PLAYING) {
                continue;
            }
            Long userId = player.getUserId();
            List<CardInfo> cards = cardMap.get(userId);

            long betAmount = betMap.getOrDefault(userId, 0L);
            long beforeGold = player.getGold() == null ? 0L : player.getGold();

            int win;
            long winAmount = 0;
            int compare = CardUtils.compare(cards, bankerCards);
            if (compare > 0) {
                win = WinState.WIN.code();
                winAmount = betAmount;
                // 翻倍
                player.addGold(betAmount * 2);
            } else if (compare == 0) {
                win = WinState.DRAW.code();
                // 归还本金
                player.addGold(betAmount);
            } else {
                win = WinState.LOSE.code();
                winAmount = -betAmount;
            }
            playerWin += winAmount;
            bankerBet += betAmount;
            HandResult handResult = CardUtils.calcHand(cards);

            long afterGold = player.getGold() == null ? beforeGold : player.getGold();
            result.add(SettlePlayerDTO.builder()
                    .userId(userId)
                    .seatId(player.getSeatId())
                    .win(win)
                    .betAmount(betAmount)
                    .winAmount(winAmount)
                    .beforeGold(beforeGold)
                    .afterGold(afterGold)
                    .cards(cards)
                    .cardTypeName(handResult.getName())
                    .settleDesc(calcDesc(winAmount).getDesc())
                    .build());
        }
        // 结算庄家
        HandResult handResult = CardUtils.calcHand(bankerCards);
        long bankerWinAmount = -playerWin;
        int bankerWinFlag = 1;
        if(bankerWinAmount < 0){
            bankerWinFlag = 0;
        }else if(bankerWinAmount > 0){
            bankerWinFlag = 2;
        }

        PaiJiuPlayer bankerPlayer = players.get(bankerUserId);
        long beforeGold = bankerPlayer.getGold();
        bankerPlayer.setGold(beforeGold + bankerWinAmount);
        long afterGold = bankerPlayer.getGold() == null ? beforeGold : bankerPlayer.getGold();
        result.add(SettlePlayerDTO.builder()
                .userId(bankerPlayer.getUserId())
                .seatId(bankerPlayer.getSeatId())
                .win(bankerWinFlag)
                .betAmount(bankerBet)
                .winAmount(bankerWinAmount)
                .beforeGold(beforeGold)
                .afterGold(afterGold)
                .cards(bankerCards)
                .cardTypeName(handResult.getName())
                .settleDesc(calcDesc(bankerWinAmount).getDesc())
                .build());

        state = RoomState.SETTLE;
        settlePush = SettlePush.builder()
                .roomId(roomId)
                .roomState(state.code())
                .bankerSeat(bankerSeat)
                .players(result)
                .build();
        return settlePush;
    }

    public SettleDescType calcDesc(long winGold) {

        if (winGold <= 0) {
            return SettleDescType.LOSE;
        }

        if (winGold < 100) {
            return SettleDescType.SMALL_WIN;
        }

        if (winGold < 1000) {
            return SettleDescType.BIG_WIN;
        }
        return SettleDescType.SUPER_WIN;
    }

    public Integer getSeatId(Long userId) {
        PaiJiuPlayer player = players.get(userId);
        return player == null ? null : player.getSeatId();
    }

    public List<PlayerDTO> getPlayerDTOList() {
        return players.values()
                .stream()
                .map(PaiJiuPlayer::toDTO)
                .collect(Collectors.toList());
    }

    public Integer findEmptySeat() {
        for (int i = 0; i < maxSeat; i++) {
            if (!seats.containsKey(i)) {
                return i;
            }
        }
        return null;
    }

    public long getPlayingCount() {
        return players.values().stream()
                .filter(p -> p.getState() == PlayerState.PLAYING)
                .count();
    }

    public long getPlayerCount() {
        return players.size();
    }

    public void selectBanker() {
        List<PaiJiuPlayer> list = players.values().stream()
                .filter(p -> p.getSeatId() >= 0)
                .toList();

        if (list.isEmpty()) return;

        int index = new Random().nextInt(list.size());
        this.bankerSeat = list.get(index).getSeatId();
    }

    public synchronized long nextRound(Long rId) {
        if(rId == roundId){
            // 1. 局号 +1
            roundId++;
            // 2. 清空上一局数据
            betMap.clear();
            cardMap.clear();
            settlePush = null;
            bankerSeat = -1;
            // 3. 玩家状态重置
            for (PaiJiuPlayer p : players.values()) {
                if (p.getSeatId() >= 0) {
                    p.setState(PlayerState.SIT); // 或 READY
                }
            }
            // 4. 状态切回等待
            state = RoomState.WAIT;
        }
        return roundId;
    }

    public synchronized void startBetCountdown(String gatewayId, Handler betHandler) {
        this.betEndTime = System.currentTimeMillis() + betSeconds * 1000L;
        if(scheduledFuture == null){
            scheduledFuture = DelayTaskUtil.getInstance().schedule(() -> {
                try {

                    for (PaiJiuPlayer player : this.players.values()) {
                        if (bankerSeat.intValue() != player.getSeatId().intValue() && !this.betMap.containsKey(player.getUserId())) {
                            BetReq betReq = new BetReq();
                            betReq.setRoomId(roomId);
                            betReq.setChip(baseScore);
                            GameRequest gameRequest = GameRequest.builder()
                                    .roomId(roomId)
                                    .userId(player.getUserId())
                                    .gatewayId(gatewayId)
                                    .data(JSONObject.from(betReq))
                                    .build();
                            betHandler.exec(gameRequest);
                            log.info("玩家自动下注 roomId={}, userId={}, bet={}", this.roomId, player.getUserId(), baseScore);
                        }
                    }
                } catch (Exception e) {
                    log.error("投注倒计时结束处理异常 roomId={}", this.roomId, e);
                }finally {
                    scheduledFuture = null;
                }
            }, betSeconds, TimeUnit.SECONDS);
        }
    }


}

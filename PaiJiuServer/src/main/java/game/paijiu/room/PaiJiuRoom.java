package game.paijiu.room;


import game.common.constant.PlayerState;
import game.common.constant.RoomState;
import game.common.entity.PlayerDTO;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
public class PaiJiuRoom {

    private Long roomId;

    private int maxSeat = 8;

    private Long ownerUserId;

    private RoomState state = RoomState.WAIT;

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
    private Map<Long, Integer> betMap = new ConcurrentHashMap<>();

    public PaiJiuRoom(Long roomId, int maxSeat) {
        this.roomId = roomId;
        this.maxSeat = maxSeat;
    }

    public synchronized PaiJiuPlayer enter(PlayerDTO info) {
        PaiJiuPlayer old = players.get(info.getUserId());
        if (old != null) {
            old.setOnline(true);
            return old;
        }

        if (players.isEmpty()) {
            ownerUserId = info.getUserId();
        }

        PaiJiuPlayer player = new PaiJiuPlayer();
        player.setUserId(info.getUserId());
        player.setSeatId(-1);
        player.setState(PlayerState.NONE);
        player.setOnline(true);
        player.setNickname(info.getNickname());
        player.setGold(info.getGold());
        player.setAvatar(info.getAvatar());

        players.put(info.getUserId(), player);
        return player;
    }

    public synchronized PaiJiuPlayer sitDown(Long userId, Integer seatId) {
        PaiJiuPlayer player = players.get(userId);
        if (player == null) {
            throw new RuntimeException("玩家不在房间");
        }

        if (player.getSeatId() != null && player.getSeatId() >= 0) {
            throw new RuntimeException("玩家已入座");
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

        player.setSeatId(finalSeatId);
        player.setState(PlayerState.SIT);

        seats.put(finalSeatId, userId);

        return player;
    }

    public synchronized void leave(Long userId) {
        PaiJiuPlayer player = players.remove(userId);
        if (player == null) {
            return;
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
            throw new RuntimeException("当前状态不能准备");
        }

        player.setState(PlayerState.READY);
        state = RoomState.READY;

        return player;
    }

    public synchronized boolean isAllReady() {
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

    public synchronized int bet(Long userId, int chip) {
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

        return betMap.merge(userId, chip, Integer::sum);
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

    private Integer findEmptySeat() {
        for (int i = 0; i < maxSeat; i++) {
            if (!seats.containsKey(i)) {
                return i;
            }
        }
        return null;
    }
}

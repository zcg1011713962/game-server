package game.common.entity;

import game.common.constant.RoomState;
import game.common.entity.res.SettlePush;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomDTO {
    // 当前局号
    private long roundId = 1;

    private Long roomId;

    private int maxSeat = 8;
    // 房主
    private Long ownerUserId;
    // 庄家
    private Integer bankerSeat;

    private long baseScore = 0;

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
    private Map<Long, Long> betMap = new ConcurrentHashMap<>();
    /**
     * userId -> 手牌
     */
    private Map<Long, List<CardInfo>> cardMap = new ConcurrentHashMap<>();

    // 结算结果
    private SettlePush settlePush;
}

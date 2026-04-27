package game.common.entity;

import lombok.Data;

import java.util.List;

@Data
public class Room {

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 房间状态
     * 0=等待 1=下注 2=发牌 3=结算
     */
    private int status;

    /**
     * 座位数量（例如8人桌）
     */
    private int seatCount;

    /**
     * 玩家列表
     */
    private List<Player> players;

    /**
     * 当前局号
     */
    private long roundId;
}

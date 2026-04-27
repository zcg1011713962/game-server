package game.common.entity;

import lombok.Data;

@Data
public class Bet {

    /**
     * 玩家ID
     */
    private Long userId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 座位号
     */
    private Integer seatId;

    /**
     * 下注金额（筹码）
     */
    private int chip;

    /**
     * 下注区域（牌九可能有多个区域）
     */
    private int betArea;

    /**
     * 时间戳
     */
    private long time;
}

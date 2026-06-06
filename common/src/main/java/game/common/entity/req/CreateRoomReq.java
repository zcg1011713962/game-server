package game.common.entity.req;

import lombok.Data;

@Data
public class CreateRoomReq {
    /**
     * 游戏ID
     */
    private Integer gameId;

    /**
     * 对局数
     * 8 16 24 32
     */
    private Integer roundCount;

    /**
     * 玩家人数
     * 4 6 8
     */
    private Integer playerCount;

    /**
     * 庄家模式
     * 1轮庄
     * 2抢庄
     * 3房主坐庄
     */
    private Integer bankerMode;

    /**
     * 特殊牌型
     */
    private Boolean zhiZun;

    private Boolean doubleTian;

    private Boolean doubleDi;

    private Boolean doubleRen;

    private Boolean doubleE;
}

package game.common.entity;

import lombok.Data;

@Data
public class Player {

    /**
     * 玩家ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 金币/余额
     */
    private long gold;

    /**
     * 所在房间
     */
    private Long roomId;

    /**
     * 座位号
     */
    private Integer seatId;

    /**
     * 是否准备
     */
    private boolean ready;

    /**
     * 是否在线
     */
    private boolean online;
}
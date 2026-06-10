package game.common.entity;

import game.common.constant.PlayerState;
import lombok.Data;

@Data
public class PaiJiuPlayer {
    private Long userId;
    private String avatar;
    private String nickname;
    private Integer seatId = -1;
    private Long gold;
    private PlayerState state = PlayerState.NONE;
    // 坐下时间
    private long sitDownTime;
    private boolean online = true;
    // null = 未操作
    // 0 = 不抢
    // 1 = 抢庄
    private Integer grabBanker;

    public void resetGrabBanker() {
        this.grabBanker = null;
    }

    public PlayerDTO toDTO() {
        PlayerDTO dto = new PlayerDTO();
        dto.setUserId(userId);
        dto.setAvatar(avatar);
        dto.setNickname(nickname);
        dto.setSeatId(seatId);
        dto.setState(state.code());
        dto.setGold(gold);
        return dto;
    }
}

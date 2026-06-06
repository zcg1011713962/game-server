package game.common.entity;

import game.common.constant.PlayerState;
import lombok.Data;

@Data
public class PaiJiuPlayer {
    private Long userId;
    private String avatar;
    private String nickname;
    private Integer seatId = -1;
    private PlayerState state = PlayerState.NONE;
    // 坐下时间
    private long sitDownTime;
    private boolean online = true;

    public PlayerDTO toDTO() {
        PlayerDTO dto = new PlayerDTO();
        dto.setUserId(userId);
        dto.setAvatar(avatar);
        dto.setNickname(nickname);
        dto.setSeatId(seatId);
        dto.setState(state.code());
        return dto;
    }
}

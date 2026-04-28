package game.paijiu.room;

import game.common.constant.PlayerState;
import game.common.entity.PlayerDTO;
import lombok.Data;

@Data
public class PaiJiuPlayer {

    private Long userId;

    private String avatar;
    private Long gold;
    private String nickname;

    private Integer seatId = -1;

    private PlayerState state = PlayerState.NONE;

    private boolean online = true;


    public PlayerDTO toDTO() {
        PlayerDTO dto = new PlayerDTO();
        dto.setUserId(userId);
        dto.setSeatId(seatId);
        dto.setState(state.code());
        dto.setOnline(online);
        dto.setAvatar(avatar);
        dto.setGold(gold);
        dto.setNickname(nickname);
        return dto;
    }
}

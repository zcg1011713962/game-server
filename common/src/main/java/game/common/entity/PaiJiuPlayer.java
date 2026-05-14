package game.common.entity;

import game.common.constant.PlayerState;
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

    public synchronized void addGold(long amount){
        this.gold += amount;
    }

    public synchronized void reduceGold(long amount){
        this.gold -= amount;
    }

    public synchronized void setGold(Long gold) {
        this.gold = gold;
    }

    public PlayerDTO toDTO() {
        PlayerDTO dto = new PlayerDTO();
        dto.setUserId(userId);
        dto.setAvatar(avatar);
        dto.setGold(gold);
        dto.setNickname(nickname);
        dto.setSeatId(seatId);
        dto.setState(state.code());
        return dto;
    }
}

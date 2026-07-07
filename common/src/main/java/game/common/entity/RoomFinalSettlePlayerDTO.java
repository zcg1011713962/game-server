package game.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomFinalSettlePlayerDTO {
    private Long userId;
    private Integer seatId;
    private String nickname;
    private String avatar;
    private Long totalWinAmount;
    private Integer bankerCount;
    private Long afterGold;
}
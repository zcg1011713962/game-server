package game.common.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SettlePlayerDTO {

    private Long userId;

    private Integer seatId;

    /**
     * 0=输 1=平 2=赢 3=庄家
     */
    private Integer win;

    private Integer betAmount;

    private Integer winAmount;

    private Long beforeGold;

    private Long afterGold;

    private List<CardInfo> cards;
}

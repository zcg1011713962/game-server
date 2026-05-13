package game.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlePlayerDTO {

    private Long userId;

    private Integer seatId;

    /**
     * 0=输 1=平 2=赢
     */
    private Integer win;

    private Long betAmount;

    private Long winAmount;

    private Long beforeGold;

    private Long afterGold;

    private List<CardInfo> cards;

    private String cardTypeName;

    private String settleDesc;
}

package game.common.entity.res;

import lombok.Data;

@Data
public class PlayerBetPush {

    private Long roomId;

    private Long userId;

    private Integer seatId;

    private Integer betArea;

    private Integer chip;

    private Integer totalBet;
}

package game.common.entity.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerOpenCardPush {

    private Long roomId;

    private Long roundId;
    private Long userId;

    private Integer seatId;

    private Integer openType;

    private Integer roomState;

    private Long serverTime;
}

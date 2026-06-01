package game.common.entity.res;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class PlayerReadyPush {

    private Long roomId;

    private Long userId;

    private Integer seatId;

    private Integer state;

    private Integer roomStatus;
}

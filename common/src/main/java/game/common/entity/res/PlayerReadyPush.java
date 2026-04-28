package game.common.entity.res;

import lombok.Data;

@Data
public class PlayerReadyPush {

    private Long roomId;

    private Long userId;

    private Integer seatId;

    private Integer state;
}

package game.common.entity.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerLeaveSeatPush {

    private Long roomId;

    private Long userId;

    private Integer seatId;

    /**
     * 离座原因
     * 0 主动离座
     * 1 超时未准备自动离座
     */
    private Integer reason;
}

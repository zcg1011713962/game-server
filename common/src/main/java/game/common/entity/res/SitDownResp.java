package game.common.entity.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SitDownResp {
    private Long roomId;
    private Long userId;
    private Integer seatId;
    private Integer state;
}

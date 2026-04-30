package game.common.entity.res;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class NextRoundPush {
    private Long roomId;
    private Long roundId;
}

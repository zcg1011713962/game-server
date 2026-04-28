package game.common.entity.res;

import lombok.Builder;
import lombok.Data;
@Builder
@Data
public class EnterRoomResp {
    private Long roomId;
    private Long userId;
}

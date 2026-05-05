package game.common.entity.res;

import game.common.constant.RoomState;
import game.common.entity.PlayerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class NextRoundPush {
    private Long roomId;
    private Long roundId;
    private Integer roomState;
    private List<PlayerDTO> players;
}

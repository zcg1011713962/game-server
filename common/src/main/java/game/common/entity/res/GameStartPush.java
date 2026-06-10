package game.common.entity.res;

import game.common.entity.PlayerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GameStartPush {

    private Long roomId;

    private Long roundId;

    private List<PlayerDTO> players;

    private Long serverTime;

    private Long roundAnimStartTime;

    private Long roundAnimEndTime;
}

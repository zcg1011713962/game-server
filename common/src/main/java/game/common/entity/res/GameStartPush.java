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

    private Integer roomState;

    private Integer bankerSeat;

    private List<PlayerDTO> players;

    private Integer betSeconds;

    private Long serverTime;

    private Long betStartTime;

    private Long betEndTime;

    private Long roundAnimStartTime;

    private Long roundAnimExpireTime;
}

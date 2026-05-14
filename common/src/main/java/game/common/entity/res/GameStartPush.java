package game.common.entity.res;

import game.common.entity.PlayerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class GameStartPush {

    private Long roomId;

    private Integer roomState;

    private Integer bankerSeat;

    private List<PlayerDTO> players;

    private Integer betSeconds;
}

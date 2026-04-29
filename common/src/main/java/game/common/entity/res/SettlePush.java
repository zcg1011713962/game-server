package game.common.entity.res;

import game.common.entity.SettlePlayerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SettlePush {

    private Long roomId;

    private Integer roomState;

    private Integer bankerSeat;

    private List<SettlePlayerDTO> players;
}

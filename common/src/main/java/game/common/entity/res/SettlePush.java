package game.common.entity.res;

import game.common.entity.SettlePlayerDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlePush {

    private Long roomId;

    private Integer roomState;

    private Integer bankerSeat;

    private List<SettlePlayerDTO> players;
}

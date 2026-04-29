package game.common.entity.res;

import game.common.entity.PlayerCardDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DealCardPush {

    private Long roomId;

    private Integer roomState;

    private Integer bankerSeat;
    /**
     * 每个玩家的牌
     */
    private List<PlayerCardDTO> playerCards;
}
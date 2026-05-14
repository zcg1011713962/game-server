package game.common.entity.res;

import game.common.entity.PlayerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PlayerBetPush {

    private Long roomId;

    private Long userId;

    private Integer seatId;

    private Integer betArea;

    private Long chip;

    private Long totalBet;

    private List<PlayerDTO> players;

}

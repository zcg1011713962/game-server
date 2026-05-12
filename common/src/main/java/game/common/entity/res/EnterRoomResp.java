package game.common.entity.res;

import game.common.entity.CardInfo;
import game.common.entity.PlayerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class EnterRoomResp {
    private Long roundId;
    private Long roomId;
    private Long userId;
    private Integer roomState;
    private Long baseScore;
    private List<PlayerDTO> players;
    private Map<String, Long> seats;
    private Map<String, Integer> betMap;
    private Map<String, List<CardInfo>> cardMap;
    private SettlePush settlePush;
    private Integer bankerSeat;
}

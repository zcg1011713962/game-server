package game.common.entity.res;

import game.common.entity.CardInfo;
import game.common.entity.PlayerDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private Map<String, Long> betMap;
    private Map<String, List<CardInfo>> cardMap;
    private Set<Long> openedCardUsers;
    private SettlePush settlePush;
    private Integer bankerSeat;
    private Long serverTime;
    private Long roundAnimStartTime;
    private Long roundAnimEndTime;
    private Long grabStartTime;
    private Long grabEndTime;
    private Long bankerAnimStartTime;
    private Long bankerAnimEndTime;
    private Long betStartTime;
    private Long betEndTime;
    private Long dealStartTime;
    private Long showCardTime;
    private Long settleTime;
    private Long nextRoundTime;
}

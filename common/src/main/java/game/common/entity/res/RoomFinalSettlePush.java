package game.common.entity.res;

import game.common.entity.RoomFinalSettlePlayerDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomFinalSettlePush {
    private Long roomId;
    private Long roundId;
    private Long roundCount;
    private Integer roomType;
    private Boolean scoreMode;
    private Long serverTime;
    private String message;
    private List<RoomFinalSettlePlayerDTO> players;
}

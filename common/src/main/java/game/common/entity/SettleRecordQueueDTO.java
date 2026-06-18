package game.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 结算记录队列DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettleRecordQueueDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -558777574487029894L;
    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 局号
     */
    private Long roundId;

    /**
     * 庄家用户ID
     */
    private Long bankerUserId;

    /**
     * 庄家座位
     */
    private Integer bankerSeat;

    /**
     * 结算时间
     */
    private Long settleTime;

    /**
     * 玩家结算数据
     */
    private List<SettlePlayerDTO> settlePlayers;
}

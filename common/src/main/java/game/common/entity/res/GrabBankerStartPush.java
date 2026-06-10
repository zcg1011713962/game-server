package game.common.entity.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrabBankerStartPush {

    private Long roomId;

    private Long roundId;

    /**
     * 房间状态
     */
    private Integer roomState;

    /**
     * 服务器当前时间
     */
    private Long serverTime;

    /**
     * 抢庄开始时间
     */
    private Long grabStartTime;

    /**
     * 抢庄结束时间
     */
    private Long grabEndTime;
}

package game.common.entity.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrabBankerPush {

    private Long roomId;

    private Long userId;

    private Integer seatId;

    /**
     * 0=不抢
     * 1=抢庄
     */
    private Integer grabBanker;

    /**
     * 房间状态
     */
    private Integer roomState;

    /**
     * 服务器时间
     */
    private Long serverTime;
}
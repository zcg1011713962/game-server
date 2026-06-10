package game.common.entity.res;

import game.common.entity.PlayerDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrabBankerResultPush {

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 局号
     */
    private Long roundId;

    /**
     * 房间状态
     */
    private Integer roomState;

    /**
     * 庄家用户ID
     */
    private Long bankerUserId;

    /**
     * 庄家座位号
     */
    private Integer bankerSeat;

    /**
     * 服务器时间
     */
    private Long serverTime;

    /**
     * 庄家动画开始时间
     */
    private Long bankerAnimStartTime;

    /**
     * 庄家动画结束时间
     */
    private Long bankerAnimExpireTime;

    /**
     * 投注开始
     */
    private Long betStartTime;

    /**
     * 投注结束
     */
    private Long betEndTime;

    /**
     * 玩家列表
     */
    private List<PlayerDTO> players;
}

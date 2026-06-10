package game.common.entity.req;


import lombok.Data;

import java.io.Serializable;

@Data
public class GrabBankerReq implements Serializable {

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 抢庄状态
     * 0 = 不抢
     * 1 = 抢庄
     */
    private Integer grabBanker;
}

package game.hall.entity.req;

import lombok.Data;

@Data
public class SettleRecordReq {
    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 20;

    private Integer roomId;
}

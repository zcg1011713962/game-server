package game.common.entity.req;

import lombok.Data;

@Data
public class SitDownReq {
    private Long roomId;

    /**
     * 可选：客户端指定座位
     * null = 服务端自动分配空座
     */
    private Integer seatId;
}

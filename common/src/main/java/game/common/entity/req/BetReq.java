package game.common.entity.req;

import lombok.Data;

@Data
public class BetReq {

    private Long roomId;

    private Integer betArea;

    private Integer chip;
}
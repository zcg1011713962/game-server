package game.hall.entity.res;

import lombok.Data;

@Data
public class SettleRecordVO {

    private Long roundId;

    private Integer win;

    private Long betAmount;

    private Long winAmount;

    private String cardTypeName;

    private String settleDesc;

    private String cards;

    private Long settleTime;
}
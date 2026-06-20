package game.hall.entity.res;

import game.common.entity.CardInfo;
import lombok.Data;

import java.util.List;

@Data
public class SettleRecordVO {

    private Long roundId;

    private Integer win;

    private Long betAmount;

    private Long winAmount;

    private String cardTypeName;

    private String settleDesc;

    private List<CardInfo> cards;

    private Long settleTime;
}
package game.hall.entity.res;

import game.common.entity.CardInfo;
import lombok.Data;

import java.util.List;

@Data
public class SettleRecordVO {

    private Long roomId;

    private Long roundId;

    private Long roundCount;

    private Long bankerCount;

    private Integer win;

    private Long betAmount;

    private Long winAmount;

    private String cardTypeName;

    private String bankerCardTypeName;

    private String settleDesc;

    private List<CardInfo> cards;

    private List<CardInfo> bankerCards;

    private Long settleTime;

    private Long startTime;

    private Long endTime;

    private Long duration;
}

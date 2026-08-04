package game.hall.entity.req;

import game.common.entity.CardInfo;
import lombok.Data;

import java.util.List;

@Data
public class TestSettleRecordReq {
    private Long userId;

    private Long bankerUserId;

    private Long gameId = 1L;

    private Integer roomId;

    private Integer roundCount = 8;

    private Long betAmount = 10L;

    private Long startTime;

    private List<Round> rounds;

    @Data
    public static class Round {
        private Long roundId;

        private Integer win;

        private Long betAmount;

        private Long winAmount;

        private String cardTypeName;

        private String settleDesc;

        private List<CardInfo> cards;

        private String bankerCardTypeName;

        private List<CardInfo> bankerCards;

        private Long settleTime;
    }
}

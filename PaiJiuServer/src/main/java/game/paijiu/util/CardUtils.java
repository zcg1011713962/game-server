package game.paijiu.util;

import game.paijiu.config.CardConfig;
import game.common.entity.CardInfo;
import game.common.entity.HandResult;

import java.util.*;

public class CardUtils {

    private CardUtils() {}

    public static List<CardInfo> getDeck() {
        return CardConfig.getDeck();
    }

    public static List<CardInfo> shuffle(List<CardInfo> deck) {
        List<CardInfo> arr = new ArrayList<>(deck);
        Collections.shuffle(arr);
        return arr;
    }

    /**
     * 发牌：每人2张
     */
    public static List<List<CardInfo>> deal(long playerCount) {
        List<CardInfo> deck = shuffle(getDeck());

        if (playerCount * 2 > deck.size()) {
            throw new RuntimeException("人数过多，牌不够发");
        }

        List<List<CardInfo>> hands = new ArrayList<>();

        for (int i = 0; i < playerCount; i++) {
            List<CardInfo> hand = new ArrayList<>();
            hand.add(deck.get(i * 2));
            hand.add(deck.get(i * 2 + 1));
            hands.add(hand);
        }

        return hands;
    }

    /**
     * 计算点数：取个位
     */
    public static int calcPoint(List<CardInfo> cards) {
        checkHand(cards);
        return (cards.get(0).getValue() + cards.get(1).getValue()) % 10;
    }

    /**
     * 是否对子
     */
    public static boolean isPair(List<CardInfo> cards) {
        checkHand(cards);
        return Objects.equals(cards.get(0).getName(), cards.get(1).getName());
    }

    /**
     * 单张牌大小
     */
    public static int getCardRank(CardInfo card) {
        return card.getId();
    }

    /**
     * 最大单张牌
     */
    public static int getMaxCardRank(List<CardInfo> cards) {
        checkHand(cards);
        return Math.max(
                getCardRank(cards.get(0)),
                getCardRank(cards.get(1))
        );
    }

    /**
     * 计算牌型
     */
    public static HandResult calcHand(List<CardInfo> cards) {
        checkHand(cards);

        int point = calcPoint(cards);

        if (isPair(cards)) {
            return new HandResult(
                    cards,
                    100 + getMaxCardRank(cards),
                    point,
                    "对子-" + cards.get(0).getName()
            );
        }

        return new HandResult(
                cards,
                point,
                point,
                point + "点"
        );
    }

    /**
     * 比牌
     * > 0 a赢
     * < 0 b赢
     * = 0 平局
     */
    public static int compare(List<CardInfo> a, List<CardInfo> b) {
        HandResult ra = calcHand(a);
        HandResult rb = calcHand(b);

        if (ra.getType() != rb.getType()) {
            return ra.getType() - rb.getType();
        }

        return getMaxCardRank(a) - getMaxCardRank(b);
    }

    public static boolean isWin(List<CardInfo> a, List<CardInfo> b) {
        return compare(a, b) > 0;
    }

    private static void checkHand(List<CardInfo> cards) {
        if (cards == null || cards.size() != 2) {
            throw new RuntimeException("一手牌必须是2张");
        }
    }
}

package game.paijiu.util;

import game.common.constant.PaiJiuType;
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

        PaiJiuType type = getPaiJiuType(cards);

        int point = calcPoint(cards);

        return switch (type) {
            case ZHI_ZUN, DOUBLE_TIAN, DOUBLE_DI, DOUBLE_REN, DOUBLE_E -> new HandResult(
                    cards,
                    type.getRank(),
                    point,
                    type.getName()
            );
            case PAIR -> new HandResult(
                    cards,
                    700 + getMaxCardRank(cards),
                    point,
                    "对子-" + cards.get(0).getName()
            );
            default -> new HandResult(
                    cards,
                    point,
                    point,
                    point + "点"
            );
        };
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


    public static PaiJiuType getPaiJiuType(List<CardInfo> cards) {

        checkHand(cards);

        CardInfo c1 = cards.get(0);
        CardInfo c2 = cards.get(1);

        String n1 = c1.getName();
        String n2 = c2.getName();

        // 至尊
        if (isNames(cards, "丁三", "大头六")) {
            return PaiJiuType.ZHI_ZUN;
        }

        // 双天
        if (n1.equals("天牌") && n2.equals("天牌")) {
            return PaiJiuType.DOUBLE_TIAN;
        }

        // 双地
        if (n1.equals("地牌") && n2.equals("地牌")) {
            return PaiJiuType.DOUBLE_DI;
        }

        // 双人
        if (n1.equals("人牌") && n2.equals("人牌")) {
            return PaiJiuType.DOUBLE_REN;
        }

        // 双鹅
        if (n1.equals("和牌") && n2.equals("和牌")) {
            return PaiJiuType.DOUBLE_E;
        }

        // 普通对子
        if (isPair(cards)) {
            return PaiJiuType.PAIR;
        }

        return PaiJiuType.POINT;
    }


    private static boolean isNames(List<CardInfo> cards, String a, String b) {

        String n1 = cards.get(0).getName();
        String n2 = cards.get(1).getName();

        return (n1.equals(a) && n2.equals(b))
                || (n1.equals(b) && n2.equals(a));
    }
}

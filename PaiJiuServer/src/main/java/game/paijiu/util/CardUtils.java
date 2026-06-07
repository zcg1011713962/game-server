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

    public static int calcPoint(List<CardInfo> cards) {
        checkHand(cards);
        return (cards.get(0).getValue() + cards.get(1).getValue()) % 10;
    }

    public static boolean isPair(List<CardInfo> cards) {
        checkHand(cards);
        return Objects.equals(cards.get(0).getName(), cards.get(1).getName());
    }

    public static int getCardRank(CardInfo card) {
        if (card == null || card.getName() == null) {
            return 0;
        }

        return switch (card.getName()) {
            case "天牌" -> 17;
            case "地牌" -> 16;
            case "人牌" -> 15;
            case "和牌" -> 14;
            case "梅花" -> 13;
            case "长三" -> 12;
            case "板凳" -> 11;
            case "斧头" -> 10;
            case "红头十" -> 9;
            case "高脚七" -> 8;
            case "铜锤六" -> 7;
            case "九点" -> 6;
            case "八点" -> 5;
            case "七点" -> 4;
            case "五点" -> 3;
            case "大头六" -> 2;
            case "丁三" -> 1;
            default -> 0;
        };
    }

    public static int getMaxCardRank(List<CardInfo> cards) {
        checkHand(cards);
        return Math.max(getCardRank(cards.get(0)), getCardRank(cards.get(1)));
    }

    public static HandResult calcHand(List<CardInfo> cards) {
        checkHand(cards);

        PaiJiuType type = getPaiJiuType(cards);
        int point = calcPoint(cards);

        return switch (type) {
            case ZHI_ZUN, DOUBLE_TIAN, DOUBLE_DI, DOUBLE_REN, DOUBLE_HE -> new HandResult(
                    cards,
                    type.getRank(),
                    point,
                    type.getName()
            );

            case PAIR -> new HandResult(
                    cards,
                    type.getRank() + getMaxCardRank(cards),
                    point,
                    type.getName() + "-" + cards.get(0).getName()
            );

            case POINT -> new HandResult(
                    cards,
                    point,
                    point,
                    point + "点"
            );
        };
    }

    public static int compare(List<CardInfo> a, List<CardInfo> b) {
        HandResult ra = calcHand(a);
        HandResult rb = calcHand(b);

        if (ra.getType() != rb.getType()) {
            return Integer.compare(ra.getType(), rb.getType());
        }

        PaiJiuType type = getPaiJiuType(a);

        if (type == PaiJiuType.POINT) {
            return 0; // 普通点数相同直接平
        }

        return Integer.compare(getMaxCardRank(a), getMaxCardRank(b));
    }


    public static PaiJiuType getPaiJiuType(List<CardInfo> cards) {
        checkHand(cards);

        String n1 = cards.get(0).getName();
        String n2 = cards.get(1).getName();

        if (isNames(cards, "丁三", "大头六")) {
            return PaiJiuType.ZHI_ZUN;
        }

        if (n1.equals("天牌") && n2.equals("天牌")) {
            return PaiJiuType.DOUBLE_TIAN;
        }

        if (n1.equals("地牌") && n2.equals("地牌")) {
            return PaiJiuType.DOUBLE_DI;
        }

        if (n1.equals("人牌") && n2.equals("人牌")) {
            return PaiJiuType.DOUBLE_REN;
        }

        if (n1.equals("和牌") && n2.equals("和牌")) {
            return PaiJiuType.DOUBLE_HE;
        }

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

    private static void checkHand(List<CardInfo> cards) {
        if (cards == null || cards.size() != 2) {
            throw new RuntimeException("一手牌必须是2张");
        }

        if (cards.get(0) == null || cards.get(1) == null) {
            throw new RuntimeException("牌不能为空");
        }
    }
}
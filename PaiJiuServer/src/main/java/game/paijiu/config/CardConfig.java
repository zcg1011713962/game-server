package game.paijiu.config;

import game.common.entity.CardInfo;

import java.util.*;

public class CardConfig {

    public static final Map<Integer, CardInfo> CARD_MAP = new LinkedHashMap<>();
    static {
        add(1, "天牌", 2);
        add(2, "天牌", 2);
        add(3, "地牌", 2);
        add(4, "地牌", 2);
        add(5, "人牌", 6);
        add(6, "人牌", 6);
        add(7, "和牌", 4);
        add(8, "和牌", 4);
        add(9, "梅花", 0);
        add(10, "梅花", 0);
        add(11, "长三", 6);
        add(12, "长三", 6);
        add(13, "板凳", 4);
        add(14, "板凳", 4);
        add(15, "斧头", 9);
        add(16, "斧头", 9);
        add(17, "红头十", 0);
        add(18, "红头十", 0);
        add(19, "高脚七", 7);
        add(20, "高脚七", 7);
        add(21, "铜锤六", 6);
        add(22, "铜锤六", 6);
        add(23, "九点", 9);
        add(24, "九点", 9);
        add(25, "八点", 8);
        add(26, "八点", 8);
        add(27, "七点", 7);
        add(28, "七点", 7);
        add(29, "五点", 5);
        add(30, "五点", 5);
        add(31, "大头六", 6);
        add(32, "丁三", 3);
    }

    private static void add(int id, String name, int value) {
        CARD_MAP.put(id, new CardInfo(id, name, value));
    }

    public static CardInfo get(int id) {
        return CARD_MAP.get(id);
    }

    public static List<CardInfo> getDeck() {
        return new ArrayList<>(CARD_MAP.values());
    }
}

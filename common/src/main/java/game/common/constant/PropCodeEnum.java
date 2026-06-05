package game.common.constant;

import lombok.Getter;

@Getter
public enum PropCodeEnum {

    /**
     * 房卡
     */
    ROOM_CARD("ROOM_CARD", "房卡"),

    /**
     * 金币
     */
    GOLD("GOLD", "金币"),

    /**
     * 钻石
     */
    DIAMOND("DIAMOND", "钻石"),

    /**
     * 小喇叭
     */
    HORN("HORN", "小喇叭"),

    /**
     * 改名卡
     */
    RENAME_CARD("RENAME_CARD", "改名卡");

    private final String code;

    private final String name;

    PropCodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static PropCodeEnum getByCode(String code) {

        for (PropCodeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }

        return null;
    }
}

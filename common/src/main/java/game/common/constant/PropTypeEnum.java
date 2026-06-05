package game.common.constant;

import lombok.Getter;

@Getter
public enum PropTypeEnum {

    ROOM_CARD(1, "房卡"),
    GOLD(2, "金币"),
    DIAMOND(3, "钻石"),
    PROP(4, "道具"),
    GIFT(5, "礼包");

    private final Integer code;
    private final String name;

    PropTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}

package game.common.constant;

import lombok.Getter;

@Getter
public enum SettleDescType {

    SMALL_WIN(1, "小赢"),

    BIG_WIN(2, "大赢"),

    SUPER_WIN(3, "超级大赢"),

    ALL_KILL(4, "通杀"),

    ALL_PAY(5, "通赔"),

    DRAW(6, "和局"),

    LOSE(7, "失败");

    private final int code;
    private final String desc;

    SettleDescType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SettleDescType of(int code) {
        for (SettleDescType e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}

package game.common.constant;

import lombok.Getter;

@Getter
public enum PaiJiuType {
    ZHI_ZUN(1000, "至尊"),
    DOUBLE_TIAN(900, "双天"),
    DOUBLE_DI(890, "双地"),
    DOUBLE_REN(880, "双人"),
    DOUBLE_E(870, "双鹅"),
    TIAN_WANG(800, "天王"),
    DI_WANG(790, "地王"),
    PAIR(700, "对子"),
    POINT(0, "点数");
    private final int rank;
    private final String name;

    PaiJiuType(int rank, String name) {
        this.rank = rank;
        this.name = name;
    }

}
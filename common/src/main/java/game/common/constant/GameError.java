package game.common.constant;

import lombok.Getter;

@Getter
public enum GameError {
    ERROR1(1001, "房间不存在"),
    ERROR2(1002, "玩家不在房间中"),
    ERROR3(1003, "当前用户状态不允许坐下"),
    ERROR4(1004, "没有空座位"),
    ERROR5(1005, "座位非法"),
    ERROR6(1006, "座位已被占用"),
    ERROR7(1007, "当前房间状态不允许坐下"),
    ERROR8(1008, "玩家未入座"),
    ERROR9(1009, "当前房间状态不能准备"),
    ERROR10(1010, "当前状态不能取消准备"),
    ERROR11(1011, "当前不是下注阶段"),
    ERROR12(1012, "玩家不在游戏中"),
    ERROR13(1013, "下注金额错误"),
    ERROR14(1014, "金币不足"),
    ERROR15(1015, "当前不是结算阶段"),
    ERROR16(1016, "庄家不存在"),
    ERROR17(1017, "庄家牌不存在"),
    ERROR18(1018, "游戏进行中不能离座"),
    ERROR19(1019, "一张房卡只能开16局"),

    ;

    private final int code;
    private final String message;

    GameError(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

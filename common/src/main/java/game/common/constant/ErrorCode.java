package game.common.constant;

public enum ErrorCode {
    // ===================== 通用 =====================
    SUCCESS(0, "ok"),
    FAIL(1, "fail"),

    PARAM_ERROR(1001, "参数错误"),
    UNKNOWN_CMD(1002, "未知命令"),
    SYSTEM_ERROR(1003, "系统异常"),
    LOGIN_ERROR(1004, "登录账户密码错误"),
    CREATE_USER_ERROR(1006, "创建用户失败"),

    // ===================== 登录 =====================
    TOKEN_INVALID(2001, "token无效"),
    NOT_LOGIN(2002, "未登录"),

    // ===================== 房间 =====================
    ROOM_NOT_EXIST(3001, "房间不存在"),
    ROOM_FULL(3002, "房间已满"),
    NOT_IN_ROOM(3003, "不在房间中"),

    // ===================== 座位 =====================
    SEAT_OCCUPIED(4001, "座位已被占用"),
    SEAT_INVALID(4002, "座位非法"),

    // ===================== 游戏 =====================
    GAME_NOT_START(5001, "游戏未开始"),
    GAME_ALREADY_START(5002, "游戏已开始"),
    NOT_YOUR_TURN(5003, "未轮到你"),

    // ===================== 下注 =====================
    BET_INVALID(6001, "下注无效"),
    GOLD_NOT_ENOUGH(6002, "金币不足"),
    BET_TOO_LARGE(6003, "下注过大");

    private final int code;
    private final String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int code() {
        return code;
    }

    public String msg() {
        return msg;
    }

    public static ErrorCode fromCode(int code) {
        for (ErrorCode e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return FAIL;
    }
}

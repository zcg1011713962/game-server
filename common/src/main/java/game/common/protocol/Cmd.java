package game.common.protocol;

public enum Cmd {

    GATEWAY_REGISTER("GATEWAY_REGISTER"),

    // ================= 登录 =================
    LOGIN("LOGIN"),
    LOGIN_RESULT("LOGIN_RESULT"),

    // ================= 心跳 =================
    PING("PING"),
    PONG("PONG"),

    // ================= 房间 =================
    ENTER_ROOM("ENTER_ROOM"),
    ENTER_ROOM_RESULT("ENTER_ROOM_RESULT"),

    // ================= 游戏 =================
    READY("READY"),
    READY_RESULT("READY_RESULT"),

    // ================= 下注 =================
    BET("BET"),
    BET_RESULT("BET_RESULT"),

    // ================= 广播 =================
    PLAYER_BET("PLAYER_BET");

    private final String cmd;

    Cmd(String cmd) {
        this.cmd = cmd;
    }

    public String value() {
        return cmd;
    }

    /**
     * 字符串 → 枚举
     */
    public static Cmd from(String cmd) {
        for (Cmd c : values()) {
            if (c.cmd.equals(cmd)) {
                return c;
            }
        }
        return null;
    }
}

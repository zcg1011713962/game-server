package game.common.protocol;

public enum Cmd {

    GATEWAY_REGISTER("GATEWAY_REGISTER"),
    LOGIN_RESULT("LOGIN_RESULT"),


    PING("PING"),
    PONG("PONG"),

    CREATE_ROOM("CREATE_ROOM"),
    FREE_MATCH("FREE_MATCH"),

    ENTER_ROOM("ENTER_ROOM"),
    ENTER_ROOM_RESULT("ENTER_ROOM_RESULT"),

    PLAYER_ENTER("PLAYER_ENTER"),

    SIT_DOWN("SIT_DOWN"),
    SIT_DOWN_RESULT("SIT_DOWN_RESULT"),
    PLAYER_SIT_DOWN("PLAYER_SIT_DOWN"),

    LEAVE_ROOM("LEAVE_ROOM"),
    PLAYER_LEAVE("PLAYER_LEAVE"),

    READY("READY"),
    READY_RESULT("READY_RESULT"),
    PLAYER_READY("PLAYER_READY"),

    CANCEL_READY("CANCEL_READY"),
    CANCEL_READY_RESULT("CANCEL_READY_RESULT"),
    CANCEL_PLAYER_READY("CANCEL_PLAYER_READY"),

    GAME_START("GAME_START"),

    BET("BET"),
    BET_RESULT("BET_RESULT"),
    PLAYER_BET("PLAYER_BET"),

    DEAL_CARD("DEAL_CARD"),
    SETTLE("SETTLE"),

    ROOM_INFO("ROOM_INFO"),
    ROOM_INFO_RESULT("ROOM_INFO_RESULT"),

    NEXT_ROUND("NEXT_ROUND"),
    NEXT_ROUND_RESULT("NEXT_ROUND_RESULT");

    private final String cmd;

    Cmd(String cmd) {
        this.cmd = cmd;
    }

    public String value() {
        return cmd;
    }

    public static Cmd from(String cmd) {
        for (Cmd c : values()) {
            if (c.cmd.equals(cmd)) {
                return c;
            }
        }
        return null;
    }
}

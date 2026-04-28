package game.common.constant;

public enum RoomState {

    WAIT(0),
    READY(1),
    BET(2),
    DEAL(3),
    SETTLE(4);

    private final int code;

    RoomState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
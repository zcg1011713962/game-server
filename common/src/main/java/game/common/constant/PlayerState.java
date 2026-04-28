package game.common.constant;

public enum PlayerState {

    NONE(0),
    SIT(1),
    READY(2),
    PLAYING(3);

    private final int code;

    PlayerState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
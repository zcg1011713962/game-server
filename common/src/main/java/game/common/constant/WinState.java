package game.common.constant;

public enum WinState {

    LOSE(0), // 输
    DRAW(1), // 平
    WIN(2); // 赢

    private final int code;

    WinState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
package game.common.constant;

public enum PlayerState {

    NONE(0), // 等待
    SIT(1), // 坐下
    READY(2), // 准备
    PLAYING(3); // 游戏中

    private final int code;

    PlayerState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
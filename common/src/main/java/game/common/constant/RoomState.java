package game.common.constant;

public enum RoomState {

    WAIT(0), // 等待
    READY(1), // 准备
    BET(2), // 投注
    DEAL(3), // 发牌
    SETTLE(4); // 结算

    private final int code;

    RoomState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
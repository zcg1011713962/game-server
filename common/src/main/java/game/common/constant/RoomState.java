package game.common.constant;

public enum RoomState {

    WAIT(0), // 等待
    READY(1), // 准备
    GRAB_BANKER(2), // 抢庄
    BET(3), // 投注
    DEAL(4), // 发牌
    SETTLE(5); // 结算

    private final int code;

    RoomState(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
package game.common.constant;

public enum PushType {
    SINGLE(1), // 单人回包
    ROOM(2); // 房间广播

    private final int code;

    PushType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}

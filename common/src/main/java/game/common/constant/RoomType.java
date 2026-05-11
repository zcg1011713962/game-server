package game.common.constant;

public enum RoomType {
    FREE_MATCH(1), // 自由匹配
    LOCK_MATCH(2); // 房主模式

    private final int code;

    RoomType(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}

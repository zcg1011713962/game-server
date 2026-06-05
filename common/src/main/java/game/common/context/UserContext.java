package game.common.context;

public class UserContext {
    private static final ThreadLocal<Long> USER_ID_HOLDER =
            new ThreadLocal<>();

    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清理
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}

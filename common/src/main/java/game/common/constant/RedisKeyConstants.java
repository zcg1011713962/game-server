package game.common.constant;

public class RedisKeyConstants {

    private RedisKeyConstants() {}

    // ================= 玩家 =================

    /** 玩家基础信息（JSON） */
    public static final String PLAYER = "player:%d";

    /** 玩家金币 */
    public static final String PLAYER_GOLD = "player:gold:%d";

    /** 玩家所在房间 */
    public static final String USER_ROOM = "user:room:%d";

    /** 玩家所在网关 */
    public static final String USER_GATEWAY = "user:gateway:%d";

    /** 玩家在线状态 */
    public static final String USER_ONLINE = "user:online:%d";


    // ================= 房间 =================

    /** 房间玩家列表（Hash） userId -> PlayerDTO */
    public static final String ROOM_PLAYERS = "room:%d:players";

    /** 房间用户集合（Set） */
    public static final String ROOM_USERS = "room:%d:users";

    /** 房间状态 */
    public static final String ROOM_STATE = "room:%d:state";

    /** 房间下注数据 */
    public static final String ROOM_BET = "room:%d:bet";

    /** 房间牌数据 */
    public static final String ROOM_CARDS = "room:%d:cards";

    public static final String ROOM_SNAPSHOT = "room:snapshot:%d";

    // ================= Token =================

    /** token -> userId */
    public static final String TOKEN = "token:%s";




    // ================= 工具方法 =================

    public static String player(Long userId) {
        return String.format(PLAYER, userId);
    }

    public static String playerGold(Long userId) {
        return String.format(PLAYER_GOLD, userId);
    }

    public static String roomSnapshot(Long roomId) {
        return String.format(ROOM_SNAPSHOT, roomId);
    }

    public static String userRoom(Long userId) {
        return String.format(USER_ROOM, userId);
    }

    public static String userGateway(Long userId) {
        return String.format(USER_GATEWAY, userId);
    }

    public static String userOnline(Long userId) {
        return String.format(USER_ONLINE, userId);
    }

    public static String roomPlayers(Long roomId) {
        return String.format(ROOM_PLAYERS, roomId);
    }

    public static String roomUsers(Long roomId) {
        return String.format(ROOM_USERS, roomId);
    }

    public static String roomState(Long roomId) {
        return String.format(ROOM_STATE, roomId);
    }

    public static String roomBet(Long roomId) {
        return String.format(ROOM_BET, roomId);
    }

    public static String roomCards(Long roomId) {
        return String.format(ROOM_CARDS, roomId);
    }

    public static String token(String token) {
        return String.format(TOKEN, token);
    }
}
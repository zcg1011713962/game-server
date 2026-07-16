package game.gateway.session;

import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Map<Long, UserSession> USER_MAP = new ConcurrentHashMap<>();
    private static final Map<ChannelId, Long> CHANNEL_MAP = new ConcurrentHashMap<>();
    private static final Map<Long, Set<Long>> ROOM_USERS = new ConcurrentHashMap<>();
    private static final Map<Long, Long> USER_ROOM = new ConcurrentHashMap<>();

    public static void bind(Long userId, Channel channel) {
        UserSession old = USER_MAP.get(userId);
        UserSession session = new UserSession(userId, channel);
        USER_MAP.put(userId, session);
        CHANNEL_MAP.put(channel.id(), userId);

        if (old != null && old.getChannel() != null && old.getChannel().isActive()
                && !old.getChannel().id().equals(channel.id())) {
            ServerMsg msg = ServerMsg.info(Cmd.FORCE_LOGOUT.value(), 0, 0, "账号已在其他窗口登录");
            old.getChannel()
                    .writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(msg)))
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void bindRoom(Long userId, Long roomId) {
        USER_ROOM.put(userId, roomId);
        ROOM_USERS.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);

        UserSession session = getByUserId(userId);
        if (session != null) {
            session.setRoomId(roomId);
        }
    }

    public static void leaveRoom(Long userId) {
        Long roomId = USER_ROOM.remove(userId);
        if (roomId == null) {
            return;
        }

        Set<Long> users = ROOM_USERS.get(roomId);
        if (users != null) {
            users.remove(userId);
            if (users.isEmpty()) {
                ROOM_USERS.remove(roomId);
            }
        }

        UserSession session = getByUserId(userId);
        if (session != null) {
            session.setRoomId(null);
        }
    }

    public static void broadcastRoom(Long roomId, ServerMsg msg) {
        Set<Long> users = ROOM_USERS.get(roomId);
        if (users == null || users.isEmpty()) {
            return;
        }

        for (Long userId : users) {
            send(userId, msg);
        }
    }

    public static UserSession getByUserId(Long userId) {
        return USER_MAP.get(userId);
    }

    public static UserSession getByChannel(Channel channel) {
        Long userId = CHANNEL_MAP.get(channel.id());
        if (userId == null) {
            return null;
        }
        return USER_MAP.get(userId);
    }

    public static Long getUserIdByChannel(Channel channel) {
        return CHANNEL_MAP.get(channel.id());
    }

    public static Long getRoomId(Long userId) {
        if (userId == null) {
            return null;
        }

        return USER_ROOM.get(userId);
    }

    public static boolean remove(Channel channel) {
        Long userId = CHANNEL_MAP.remove(channel.id());
        if (userId == null) {
            return false;
        }

        UserSession session = USER_MAP.get(userId);
        if (session == null || session.getChannel() == null || !session.getChannel().id().equals(channel.id())) {
            return false;
        }

        USER_MAP.remove(userId);
        return true;
    }

    public static void send(Long userId, ServerMsg msg) {
        UserSession session = getByUserId(userId);
        if (session == null) {
            return;
        }

        Channel channel = session.getChannel();
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(msg)));
        }
    }
}

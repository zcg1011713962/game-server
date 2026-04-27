package game.gateway.session;

import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Map<Long, UserSession> USER_MAP = new ConcurrentHashMap<>();
    private static final Map<ChannelId, Long> CHANNEL_MAP = new ConcurrentHashMap<>();

    public static void bind(Long userId, Channel channel) {
        UserSession old = USER_MAP.get(userId);
        if (old != null && old.getChannel() != null && old.getChannel().isActive()) {
            old.getChannel().close();
        }
        UserSession session = new UserSession(userId, channel);
        USER_MAP.put(userId, session);
        CHANNEL_MAP.put(channel.id(), userId);
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

    public static void remove(Channel channel) {
        Long userId = CHANNEL_MAP.remove(channel.id());
        if (userId != null) {
            USER_MAP.remove(userId);
        }
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
package game.paijiu.netty;

import game.common.util.JsonUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GatewayChannelManager {
    private static final Map<String, Channel> GATEWAY_MAP = new ConcurrentHashMap<>();
    private static final Map<ChannelId, String> CHANNEL_GATEWAY_MAP = new ConcurrentHashMap<>();

    public static void bind(String gatewayId, Channel channel) {
        GATEWAY_MAP.put(gatewayId, channel);
        CHANNEL_GATEWAY_MAP.put(channel.id(), gatewayId);
    }

    public static Channel get(String gatewayId) {
        return GATEWAY_MAP.get(gatewayId);
    }

    public static String getGatewayId(Channel channel) {
        return CHANNEL_GATEWAY_MAP.get(channel.id());
    }

    public static void remove(Channel channel) {
        String gatewayId = CHANNEL_GATEWAY_MAP.remove(channel.id());
        if (gatewayId != null) {
            GATEWAY_MAP.remove(gatewayId);
        }
    }

    public static void send(String gatewayId, Object msg) {
        Channel channel = GATEWAY_MAP.get(gatewayId);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(JsonUtil.toJson(msg));
        }
    }
}

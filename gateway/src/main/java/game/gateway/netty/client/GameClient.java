package game.gateway.netty.client;

import game.common.util.JsonUtil;
import game.gateway.netty.client.handler.GameClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class GameClient {

    @Value("${game.server.host:127.0.0.1}")
    private String host;

    @Value("${game.server.port:19092}")
    private int port;

    private EventLoopGroup group;
    private Channel channel;

    private final GameClientHandler handler;

    public GameClient(GameClientHandler handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void start() {
        group = new NioEventLoopGroup();
        connect();
    }

    private void connect() {
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new GameClientInitializer(handler));

        bootstrap.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("已连接 GameServer");
                channel = future.channel();
            } else {
                log.info("GameServer连接失败，3秒后重连...");
                future.channel().eventLoop().schedule(this::connect, 3, TimeUnit.SECONDS);
            }
        });
    }

    public void send(Object obj) {
        if (channel == null || !channel.isActive()) {
            throw new RuntimeException("GameServer未连接");
        }
        channel.writeAndFlush(JsonUtil.toJson(obj));
    }
}

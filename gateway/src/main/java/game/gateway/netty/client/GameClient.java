package game.gateway.netty.client;

import game.common.entity.req.GameRequest;
import game.common.protocol.Cmd;
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
import jakarta.annotation.PreDestroy;
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
    @Value("${gateway.id:gw-0}")
    private String gatewayId;

    private EventLoopGroup group;
    private Channel channel;
    private GameClientHandler gameClientHandler;

    @PostConstruct
    public void start() {
        group = new NioEventLoopGroup();
        gameClientHandler = new GameClientHandler(this);
        connect();
    }

    private void connect() {
        Bootstrap bootstrap = new Bootstrap();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new GameClientInitializer(gameClientHandler));

        bootstrap.connect(host, port).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("已连接 GameServer");
                channel = future.channel();
                registerGateway();
            } else {
                scheduleReconnect();
            }
        });
    }

    public void scheduleReconnect() {
        if (group == null || group.isShutdown()) {
            return;
        }
        group.schedule(() -> {
            log.info("正在重连 GameServer...");
            connect();
        }, 3, TimeUnit.SECONDS);
    }

    private void registerGateway() {
        GameRequest req = new GameRequest();
        req.setCmd(Cmd.GATEWAY_REGISTER);
        req.setGatewayId(gatewayId);

        channel.writeAndFlush(JsonUtil.toJson(req));
    }

    public void send(Object obj) {
        if (channel == null || !channel.isActive()) {
            throw new RuntimeException("GameServer未连接");
        }
        channel.writeAndFlush(JsonUtil.toJson(obj));
    }

    @PreDestroy
    public void stop() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }
}

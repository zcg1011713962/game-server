package game.gomoku.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GomokuServer {

    @Value("${gomoku.port:19092}")
    private int port;

    private final GomokuInitializer initializer;

    public GomokuServer(GomokuInitializer initializer) {
        this.initializer = initializer;
    }

    public void start() throws Exception {

        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup();

        new ServerBootstrap()
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .childHandler(initializer)
                .bind(port)
                .sync();

        log.info("五子棋服务启动：{}", port);
    }
}



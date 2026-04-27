package game.paijiu.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaiJiuServer {

    @Value("${paijiu.port:19092}")
    private int port;

    private final PaiJiuInitializer initializer;

    public PaiJiuServer(PaiJiuInitializer initializer) {
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

        log.info("牌九服务启动：{}", port);
    }
}

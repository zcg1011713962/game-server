package game.gateway.netty.server;

import game.gateway.netty.server.handler.WebSocketFrameHandler;
import game.gateway.netty.server.handler.WsAuthHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class WsServerInitializer extends ChannelInitializer<SocketChannel> {

    private final WebSocketFrameHandler webSocketFrameHandler;

    public WsServerInitializer(WebSocketFrameHandler webSocketFrameHandler) {
        this.webSocketFrameHandler = webSocketFrameHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(65536));

        // 握手前鉴权
        p.addLast(new WsAuthHandler());
        // 60秒没有读到客户端消息，触发 IdleStateEvent
        p.addLast(new IdleStateHandler(60, 0, 0, TimeUnit.SECONDS));
        p.addLast(new WebSocketServerProtocolHandler("/ws", null, true));
        p.addLast(webSocketFrameHandler);
    }
}
package game.gateway.netty.client;

import game.gateway.netty.client.handler.GomokuClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.StandardCharsets;

public class GomokuClientInitializer extends ChannelInitializer<SocketChannel> {

    private final GomokuClientHandler handler;

    public GomokuClientInitializer(GomokuClientHandler handler) {
        this.handler = handler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new LengthFieldBasedFrameDecoder(
                1024 * 1024, 0, 4, 0, 4));
        p.addLast(new LengthFieldPrepender(4));
        p.addLast(new StringDecoder(StandardCharsets.UTF_8));
        p.addLast(new StringEncoder(StandardCharsets.UTF_8));
        p.addLast(handler);
    }
}


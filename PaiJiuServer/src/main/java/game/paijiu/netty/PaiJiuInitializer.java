package game.paijiu.netty;

import game.paijiu.netty.handler.GameRequestHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PaiJiuInitializer extends ChannelInitializer<SocketChannel> {

    private final GameRequestHandler gameRequestHandler;

    public PaiJiuInitializer(GameRequestHandler gameRequestHandler) {
        this.gameRequestHandler = gameRequestHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();

        // 拆包：读取 4 字节长度头
        p.addLast(new LengthFieldBasedFrameDecoder(
                1024 * 1024, // 单包最大 1MB
                0,           // 长度字段偏移
                4,           // 长度字段长度
                0,           // 长度修正
                4            // 跳过长度字段
        ));

        // 粘包：发送时自动加 4 字节长度头
        p.addLast(new LengthFieldPrepender(4));

        // 字符串编解码
        p.addLast(new StringDecoder(StandardCharsets.UTF_8));
        p.addLast(new StringEncoder(StandardCharsets.UTF_8));

        // 业务处理
        p.addLast(gameRequestHandler);
    }
}
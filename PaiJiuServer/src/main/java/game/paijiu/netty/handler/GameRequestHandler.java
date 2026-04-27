package game.paijiu.netty.handler;

import game.common.entity.req.GameRequest;
import game.common.util.JsonUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j
public class GameRequestHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String json) throws Exception {
        GameRequest req = JsonUtil.parse(json, GameRequest.class);
        log.info(req.toString());
    }
}

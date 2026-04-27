package game.gateway.netty.server.handler;

import game.common.util.JwtUtil;
import game.gateway.session.SessionKeys;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class WsAuthHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        String path = decoder.path();
        if (!"/ws".equals(path)) {
            ctx.close();
            return;
        }
        List<String> tokens = decoder.parameters().get("token");
        if (tokens == null || tokens.isEmpty()) {
            log.info("ws auth token is empty");
            ctx.close();
            return;
        }
        String token = tokens.get(0);
        Long userId;
        try {
            userId = JwtUtil.getUserId(token);
        } catch (Exception e) {
            ctx.close();
            return;
        }
        if (userId == null) {
            ctx.close();
            return;
        }
        // 先把 userId 放到Channel
        ctx.channel().attr(SessionKeys.USER_ID).set(userId);
        req.setUri(path);
        ctx.fireChannelRead(req.retain());
    }
}

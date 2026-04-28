package game.paijiu.netty.handler;

import game.common.constant.ErrorCode;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
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
    protected void channelRead0(ChannelHandlerContext ctx, String json) throws Exception {
        GameRequest req = JsonUtil.parse(json, GameRequest.class);
        if (req.getCmd().value().equals(Cmd.GATEWAY_REGISTER.value())) {
            GatewayChannelManager.bind(req.getGatewayId(), ctx.channel());
            log.info("网关注册:{}", req.getGatewayId());
            return;
        }
        if(req.getGatewayId() != null && GatewayChannelManager.get(req.getGatewayId()) != null){
            DispatcherHandler.getHandler(req.getCmd().value()).exec(req);
        }else{
            ctx.writeAndFlush(JsonUtil.toJson(GameResponse.error(req, ErrorCode.UNKNOWN_CMD)));
            log.error("非法的消息:{}", json);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("网关断开");
        GatewayChannelManager.remove(ctx.channel());
    }
}

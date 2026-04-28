package game.gateway.netty.server.handler;

import game.common.constant.ErrorCode;
import game.common.exception.BizException;
import game.common.protocol.ClientMsg;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import game.common.entity.req.GameRequest;
import game.gateway.netty.client.GameClient;
import game.gateway.session.SessionKeys;
import game.gateway.session.SessionManager;
import game.gateway.session.UserSession;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@io.netty.channel.ChannelHandler.Sharable
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    private GameClient gameClient;
    @Value("${gateway.id:gw-0}")
    private String gatewayId;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame frame) {
        ClientMsg msg = null;
        try {
            String text = frame.text();
            msg = JsonUtil.parse(text, ClientMsg.class);
            if (msg == null || msg.getCmd() == null || msg.getCmd().value() == null) {
                throw new BizException(ErrorCode.PARAM_ERROR);
            }
            UserSession userSession = SessionManager.getByChannel(ctx.channel());
            if(userSession == null){
                throw new BizException(ErrorCode.NOT_LOGIN);
            }
            // 回复PING包
            if(msg.getCmd().value().equals(Cmd.PING.value())){
                ServerMsg serverMsg = ServerMsg.ok(Cmd.PONG.value(), msg.getSeq(), msg.getData());
                ctx.channel().writeAndFlush(serverMsg);
                return;
            }
            forwardToGame(userSession, msg);
        } catch (BizException e) {
            sendError(ctx, msg, e.getCode(), e.getMsg());
        } catch (Exception e) {
            sendError(ctx, msg, ErrorCode.SYSTEM_ERROR.code(), ErrorCode.SYSTEM_ERROR.msg());
            log.error(ErrorCode.SYSTEM_ERROR.msg(), e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        UserSession userSession = SessionManager.getByChannel(ctx.channel());
        if(userSession != null){
            log.info("channelInactive userId:{}", userSession.getUserId());
        }
        SessionManager.remove(ctx.channel());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            return;
        }
        ctx.fireUserEventTriggered(evt);
    }

    private void sendError(ChannelHandlerContext ctx, ClientMsg msg, int code, String message) {
        String cmd = msg == null ? "ERROR" : msg.getCmd() + "_RESULT";
        long seq = msg == null ? 0 : msg.getSeq();

        ServerMsg res = ServerMsg.error(cmd, seq, code, message);
        ctx.channel().writeAndFlush(new TextWebSocketFrame(JsonUtil.toJson(res)));
    }

    private void forwardToGame(UserSession userSession, ClientMsg msg) {
        GameRequest req = new GameRequest();
        req.setGatewayId(gatewayId);
        req.setUserId(userSession.getUserId());
        req.setCmd(msg.getCmd());
        req.setSeq(msg.getSeq());
        req.setData(msg.getData());
        gameClient.send(req);
    }
}
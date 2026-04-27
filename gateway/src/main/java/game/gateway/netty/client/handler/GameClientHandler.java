package game.gateway.netty.client.handler;

import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import game.gateway.session.SessionManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.stereotype.Component;

@Component
@io.netty.channel.ChannelHandler.Sharable
public class GameClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {

        ServerMsg res = JsonUtil.parse(msg, ServerMsg.class);

        if (res == null) return;

        // 单人消息
        if (res.getSeq() > 0 && res.getData() != null) {
            Long userId = (Long) ((java.util.Map<?, ?>) res.getData()).get("userId");
            if (userId != null) {
                SessionManager.send(userId, res);
            }
        }

        // 广播（你可以改为 roomId）
        // TODO: 根据 roomId 广播
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("⚠️ GameServer断开连接");
    }
}

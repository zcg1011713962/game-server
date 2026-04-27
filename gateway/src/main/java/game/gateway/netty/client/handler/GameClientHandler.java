package game.gateway.netty.client.handler;

import game.common.protocol.ServerMsg;
import game.common.util.JsonUtil;
import game.gateway.netty.client.GameClient;
import game.gateway.session.SessionManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@ChannelHandler.Sharable
public class GameClientHandler extends SimpleChannelInboundHandler<String> {
    private final GameClient gameClient;
    public GameClientHandler(GameClient gameClient){
        this.gameClient = gameClient;
    }
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
        // 多服务广播（你可以改为 roomId）
        // TODO: 根据 roomId 广播
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("GameServer断开连接,准备重连");
        gameClient.scheduleReconnect();
    }
}

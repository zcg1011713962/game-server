package game.gateway.netty.client.handler;

import game.common.entity.res.GameResponse;
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
        GameResponse gameResponse = JsonUtil.parse(msg, GameResponse.class);
        if (gameResponse == null) return;
        log.info("GameServer Response:{}", gameResponse);
        // 单人消息
        if(gameResponse.getPushType() == 1){
            if(gameResponse.getUserId() != null){
                ServerMsg serverMsg = ServerMsg.ok(gameResponse.getCmd().value(), gameResponse.getSeq(), gameResponse.getData());
                SessionManager.send(gameResponse.getUserId(), serverMsg);
            }
        }else if(gameResponse.getPushType() == 2){

        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("GameServer断开连接,准备重连");
        gameClient.scheduleReconnect();
    }
}

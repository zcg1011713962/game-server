package game.paijiu.handler;

import game.common.entity.Packet;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import org.springframework.stereotype.Component;

@Component
public class PingHandler extends DispatcherHandler {
    public PingHandler() {
        super(Cmd.PING.value());
    }

    @Override
    public void exec(Packet packet) {
        ServerMsg pong = ServerMsg.ok(Cmd.PONG.value(), 0, null);
        GatewayChannelManager.send(packet.getGatewayId(), pong);
    }
}

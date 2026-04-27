package game.paijiu.handler;

import game.common.entity.Packet;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import org.springframework.stereotype.Component;

@Component
public class ReadyHandler extends DispatcherHandler {
    public ReadyHandler() {
        super(Cmd.READY.value());
    }

    @Override
    public void exec(Packet packet) {
        ServerMsg serverMsg = ServerMsg.ok(Cmd.READY.value(), 0, null);
        GatewayChannelManager.send(packet.getGatewayId(), serverMsg);
    }
}

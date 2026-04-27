package game.paijiu.handler;

import game.common.entity.Packet;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import org.springframework.stereotype.Component;

@Component
public class EnterRoomHandler extends DispatcherHandler {
    public EnterRoomHandler() {
        super(Cmd.ENTER_ROOM.value());
    }

    @Override
    public void exec(Packet packet) {
        ServerMsg serverMsg = ServerMsg.ok(Cmd.ENTER_ROOM_RESULT.value(), 0, null);
        GatewayChannelManager.send(packet.getGatewayId(), serverMsg);
    }
}

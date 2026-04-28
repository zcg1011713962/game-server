package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.Packet;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ReadyHandler extends DispatcherHandler {
    public ReadyHandler() {
        super(Cmd.READY.value());
    }

    @Override
    public void exec(GameRequest gameRequest) {
        GameResponse gameResponse = GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(gameRequest.getGatewayId())
                .pushType(1)
                .cmd(Cmd.READY_RESULT)
                .userId(gameRequest.getUserId())
                .code(ErrorCode.SUCCESS.code())
                .data(null).build();
        GatewayChannelManager.send(gameRequest.getGatewayId(), gameResponse);
    }
}

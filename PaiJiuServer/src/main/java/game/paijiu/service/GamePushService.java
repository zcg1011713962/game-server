package game.paijiu.service;

import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.entity.res.GoldChangePush;
import game.common.protocol.Cmd;
import game.paijiu.netty.GatewayChannelManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GamePushService {

    public void pushGoldChange(GameRequest req, Long roomId, Long userId, Long gold, Long changeGold, String reason) {
        GoldChangePush push = GoldChangePush.builder().userId(userId).gold(gold).changeGold(changeGold).reason(reason).build();

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(PushType.SINGLE.code())
                .cmd(Cmd.GOLD_CHANGE)
                .userId(userId)
                .roomId(roomId)
                .code(ErrorCode.SUCCESS.code())
                .data(push)
                .build());
    }
}

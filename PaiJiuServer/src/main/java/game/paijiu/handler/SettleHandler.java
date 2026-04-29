package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.req.GameRequest;
import game.common.entity.req.SettleReq;
import game.common.entity.res.GameResponse;
import game.common.entity.res.SettlePush;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SettleHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public SettleHandler() {
        super(Cmd.SETTLE.value());
    }

    @Override
    public void exec(GameRequest req) {
        // 客户端发牌动画播放完，再结算
        SettleReq data = JsonUtil.objToBean(req.getData(), SettleReq.class);

        PaiJiuRoom room = roomManager.get(data.getRoomId());
        if (room == null) {
            log.error("bet room is null");
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        // 结算
        SettlePush settlePush = room.settle();

        GatewayChannelManager.send(
                req.getGatewayId(),
                GameResponse.push(room.getRoomId(), Cmd.SETTLE, settlePush)
        );


    }
}

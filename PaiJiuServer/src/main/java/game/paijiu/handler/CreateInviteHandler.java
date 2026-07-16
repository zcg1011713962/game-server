package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.entity.req.EnterRoomReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateInviteHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public CreateInviteHandler() {
        super(Cmd.CREATE_INVITE.value());
    }

    @Override
    public void exec(GameRequest req) {
        if (req.getData() == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }
        EnterRoomReq enterRoomReq = JsonUtil.parse(req.getData().toString(), EnterRoomReq.class);
        if (enterRoomReq == null || enterRoomReq.getRoomId() == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        String invite = roomManager.createInvite(enterRoomReq.getRoomId());
        if (invite == null) {
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.ok(req, Cmd.CREATE_INVITE_RESULT,
                CreateInviteResp.builder().invite(invite).build()));
    }

    @Data
    @Builder
    public static class CreateInviteResp {
        private String invite;
    }
}

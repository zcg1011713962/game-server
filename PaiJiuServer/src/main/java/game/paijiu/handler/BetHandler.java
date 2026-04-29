package game.paijiu.handler;

import game.common.constant.ErrorCode;
import game.common.constant.PlayerState;
import game.common.constant.RoomState;
import game.common.entity.PlayerCardDTO;
import game.common.entity.req.BetReq;
import game.common.entity.req.GameRequest;
import game.common.entity.res.DealCardPush;
import game.common.entity.res.GameResponse;
import game.common.entity.res.PlayerBetPush;
import game.common.protocol.Cmd;
import game.common.util.JsonUtil;
import game.common.entity.CardInfo;
import game.paijiu.netty.GatewayChannelManager;
import game.paijiu.netty.handler.DispatcherHandler;
import game.paijiu.room.PaiJiuPlayer;
import game.paijiu.room.PaiJiuRoom;
import game.paijiu.room.PaiJiuRoomManager;
import game.paijiu.util.CardUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class BetHandler extends DispatcherHandler {
    @Autowired
    PaiJiuRoomManager roomManager;

    public BetHandler() {
        super(Cmd.BET.value());
    }

    @Override
    public void exec(GameRequest req) {
        BetReq data = JsonUtil.objToBean(req.getData(), BetReq.class);

        if (data == null || data.getRoomId() == null || data.getChip() == null) {
            log.error("bet params error");
            return;
        }
        PaiJiuRoom room = roomManager.get(data.getRoomId());
        if (room == null) {
            log.error("bet room is null");
            GatewayChannelManager.send(req.getGatewayId(), GameResponse.error(req, ErrorCode.ROOM_NOT_EXIST));
            return;
        }

        req.setRoomId(room.getRoomId());

        int totalBet = room.bet(req.getUserId(), data.getChip());
        Integer seatId = room.getSeatId(req.getUserId());

        PlayerBetPush pushData = new PlayerBetPush();
        pushData.setRoomId(room.getRoomId());
        pushData.setUserId(req.getUserId());
        pushData.setSeatId(seatId);
        pushData.setBetArea(data.getBetArea());
        pushData.setChip(data.getChip());
        pushData.setTotalBet(totalBet);

        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(1)
                .cmd(Cmd.BET_RESULT)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(pushData).build());

        // 广播
        GatewayChannelManager.send(req.getGatewayId(), GameResponse.builder()
                .traceId(UUID.randomUUID().toString())
                .gatewayId(req.getGatewayId())
                .pushType(2)
                .cmd(Cmd.PLAYER_BET)
                .userId(req.getUserId())
                .roomId(room.getRoomId())
                .code(ErrorCode.SUCCESS.code())
                .data(pushData).build());

        // 所有人都下注了，进入发牌
        if (room.canDeal()) {
            room.setState(RoomState.DEAL);
            // 1. 生成牌
            List<List<CardInfo>> hands = CardUtils.deal(room.getPlayingCount());

            // 2. 转换为DTO
            List<PlayerCardDTO> list = new ArrayList<>();
            int index = 0;
            for (PaiJiuPlayer p : room.getPlayers().values()) {
                if (p.getState() == PlayerState.PLAYING) {
                    List<CardInfo> hand = hands.get(index++);
                    // 保存手牌
                    room.getCardMap().put(p.getUserId(), hand);

                    PlayerCardDTO dto = new PlayerCardDTO();
                    dto.setUserId(p.getUserId());
                    dto.setSeatId(p.getSeatId());
                    dto.setCards(hand);
                    list.add(dto);
                }
            }

            GameResponse dealPush = GameResponse.push(room.getRoomId(), Cmd.DEAL_CARD, DealCardPush.builder()
                            .roomId(room.getRoomId())
                            .roomState(room.getState().code())
                            .bankerSeat(room.getBankerSeat())
                            .playerCards(list)
                            .build()
            );
            GatewayChannelManager.send(req.getGatewayId(), dealPush);
        }
    }
}

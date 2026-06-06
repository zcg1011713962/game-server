package game.paijiu.room;

import cn.hutool.extra.spring.SpringUtil;
import game.common.constant.AssetType;
import game.common.constant.ErrorCode;
import game.common.constant.PushType;
import game.common.entity.AssetChangeMsg;
import game.common.entity.res.AssetChangePush;
import game.common.entity.res.GameResponse;
import game.common.protocol.Cmd;
import game.common.util.RedisUtil;
import game.paijiu.netty.GatewayChannelManager;

import java.util.UUID;

public class AssetPushManager {

    /**
     * 房卡变化
     */
    public static void pushRoomCard(
            String gatewayId,
            Long userId,
            long change,
            long currentValue
    ) {

        pushAsset(
                gatewayId,
                userId,
                AssetType.ROOM_CARD.getField(),
                change,
                currentValue
        );
    }

    /**
     * 金币变化
     */
    public static void pushGold(
            String gatewayId,
            Long userId,
            long change,
            long currentValue
    ) {

        pushAsset(
                gatewayId,
                userId,
                AssetType.GOLD.getField(),
                change,
                currentValue
        );
    }

    /**
     * 通用资产推送
     */
    private static void pushAsset(
            String gatewayId,
            Long userId,
            String field,
            long change,
            long currentValue
    ) {
        RedisUtil redisUtil = SpringUtil.getBean(RedisUtil.class);
        AssetChangeMsg msg = new AssetChangeMsg();
        msg.setUserId(userId);
        msg.setField(field);
        redisUtil.convertAndSend("asset:change", msg);

        GatewayChannelManager.send(
                gatewayId,
                GameResponse.builder()
                        .traceId(UUID.randomUUID().toString())
                        .gatewayId(gatewayId)
                        .pushType(PushType.SINGLE.code())
                        .cmd(Cmd.USER_ASSET_UPDATE)
                        .userId(userId)
                        .code(ErrorCode.SUCCESS.code())
                        .data(AssetChangePush.builder()
                                        .field(field)
                                        .change(change)
                                        .value(currentValue)
                                        .build()
                        )
                        .build()
        );
    }
}

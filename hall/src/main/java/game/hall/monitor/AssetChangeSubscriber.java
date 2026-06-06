package game.hall.monitor;

import com.alibaba.fastjson2.JSON;
import game.common.constant.AssetType;
import game.common.constant.PropCodeEnum;
import game.common.entity.AssetChangeMsg;
import game.common.service.UserService;
import game.hall.service.UserBagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AssetChangeSubscriber implements MessageListener {
    @Autowired
    private UserBagService userBagService;
    @Autowired
    private UserService userService;
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        AssetChangeMsg msg = JSON.parseObject(body, AssetChangeMsg.class);
        log.info("收到频道: {}", channel);
        log.info("收到消息: {}", body);
        if("asset:change".equals(channel)){
            log.info("收到资产变更消息: {}", body);
            if (AssetType.ROOM_CARD.getField().equals(msg.getField())) {
                userBagService.updateByRedis(msg.getUserId());
            }
            if (AssetType.GOLD.getField().equals(msg.getField())) {
                userService.updateByRedis(msg.getUserId());
            }
        }
    }
}

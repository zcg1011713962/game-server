package game.paijiu.monitor;

import game.common.entity.req.RemoveRoomReq;
import game.common.util.JsonUtil;
import game.paijiu.room.PaiJiuRoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class RoomControllerSubscriber implements MessageListener {
    @Autowired
    private PaiJiuRoomManager paiJiuRoomManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String body = new String(message.getBody());
        log.info("收到频道: {}", channel);
        log.info("收到消息: {}", body);
        if("room:remove".equals(channel)){
            RemoveRoomReq removeRoomReq = JsonUtil.parse(body, RemoveRoomReq.class);
            if(removeRoomReq != null && removeRoomReq.getRoomId() != null){
                paiJiuRoomManager.remove(removeRoomReq.getRoomId());
            }
        }
    }
}

package game.paijiu.room;


import com.alibaba.fastjson2.JSONObject;
import game.common.constant.RedisKeyConstants;
import game.common.util.JsonUtil;
import game.paijiu.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaiJiuRoomManager {
    @Autowired
    private RedisUtil redisUtil;

    private final Map<Long, PaiJiuRoom> roomMap = new ConcurrentHashMap<>();

    public PaiJiuRoom getOrCreate(Long roomId) {
        return roomMap.computeIfAbsent(roomId, id -> {
            JSONObject jsonObject = redisUtil.get(RedisKeyConstants.roomSnapshot(id));
            if (jsonObject != null) {
                return JsonUtil.objToBean(jsonObject, PaiJiuRoom.class);
            }
            return new PaiJiuRoom(id, 8);
        });
    }

    /**
     * enterRoom
     * sitDown
     * ready
     * bet
     * deal
     * settle
     * leaveRoom
     */
    public void save(PaiJiuRoom room) {
        roomMap.put(room.getRoomId(), room);
        redisUtil.set(RedisKeyConstants.roomSnapshot(room.getRoomId()), room, 3600);
    }

    public void remove(Long roomId) {
        roomMap.remove(roomId);
        redisUtil.del(RedisKeyConstants.roomSnapshot(roomId));
    }

    public PaiJiuRoom get(Long roomId) {
        return roomMap.get(roomId);
    }

}

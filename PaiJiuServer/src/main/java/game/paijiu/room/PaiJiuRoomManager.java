package game.paijiu.room;


import game.common.constant.RedisKeyConstants;
import game.common.entity.RoomDTO;
import game.paijiu.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaiJiuRoomManager {
    @Autowired
    private RedisUtil redisUtil;

    private final Map<Long, PaiJiuRoom> roomMap = new ConcurrentHashMap<>();

    public PaiJiuRoom getOrCreate(Long roomId) {
        return roomMap.computeIfAbsent(roomId, id -> {
            RoomDTO roomDTO = redisUtil.get(RedisKeyConstants.roomSnapshot(id));
            if (roomDTO != null) {
                PaiJiuRoom paiJiuRoom = new PaiJiuRoom();
                BeanUtils.copyProperties(roomDTO, paiJiuRoom);
                return paiJiuRoom;
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
        redisUtil.set(RedisKeyConstants.roomSnapshot(room.getRoomId()), room.toRoomDTO(), 3600);
    }

    public void remove(Long roomId) {
        roomMap.remove(roomId);
        redisUtil.del(RedisKeyConstants.roomSnapshot(roomId));
    }

    public PaiJiuRoom get(Long roomId) {
        return roomMap.get(roomId);
    }



}

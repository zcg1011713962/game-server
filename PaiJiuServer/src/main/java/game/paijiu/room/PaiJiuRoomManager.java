package game.paijiu.room;


import game.common.constant.RedisKeyConstants;
import game.common.constant.RoomState;
import game.common.constant.RoomType;
import game.common.entity.RoomDTO;
import game.paijiu.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PaiJiuRoomManager {
    @Autowired
    private RedisUtil redisUtil;

    private final Map<Long, PaiJiuRoom> roomMap = new ConcurrentHashMap<>();

    /**
     * playerId -> roomId
     */
    private final Map<Long, Long> playerRoomMap = new ConcurrentHashMap<>();

    /**
     * 创建房间
     */
    public PaiJiuRoom createRoom(RoomType roomType) {
        Long roomId = nextRoomId();
        PaiJiuRoom room = new PaiJiuRoom(roomId, roomType, 8);
        roomMap.put(roomId, room);
        save(room);
        log.info("创建房间成功 roomId:{} roomType:{}", roomId, roomType);
        return room;
    }

    /**
     * 获取已经存在的房间
     */
    public PaiJiuRoom getRoom(Long roomId) {
        if (roomId == null) {
            return null;
        }

        PaiJiuRoom room = roomMap.get(roomId);
        if (room != null) {
            return room;
        }

        synchronized (this) {
            room = roomMap.get(roomId);
            if (room != null) {
                return room;
            }

            RoomDTO roomDTO = redisUtil.get(RedisKeyConstants.roomSnapshot(roomId));
            if (roomDTO == null) {
                return null;
            }

            PaiJiuRoom paiJiuRoom = new PaiJiuRoom();
            BeanUtils.copyProperties(roomDTO, paiJiuRoom);

            roomMap.put(roomId, paiJiuRoom);

            return paiJiuRoom;
        }
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
        log.info("解散房间:{}", roomId);
        roomMap.remove(roomId);
        redisUtil.del(RedisKeyConstants.roomSnapshot(roomId));
    }

    /**
     * 记录玩家进房
     */
    public void saveUserRoom(Long userId, Long roomId) {
        playerRoomMap.put(userId, roomId);
        redisUtil.set(RedisKeyConstants.userRoom(userId), roomId, 3600);
    }

    public void removeUserRoom(Long userId, Long roomId) {
        playerRoomMap.remove(userId, roomId);
        redisUtil.del(RedisKeyConstants.userRoom(userId));
    }

    public PaiJiuRoom get(Long roomId) {
        return roomMap.get(roomId);
    }

    public Long getRoomIdByUserId(Long userId) {
        if (playerRoomMap.containsKey(userId)) {
            return playerRoomMap.get(userId);
        }
        Long roomId = redisUtil.get(RedisKeyConstants.userRoom(userId));
        if (roomId != null) {
            playerRoomMap.put(userId, roomId);
            return roomId;
        }
        return null;
    }

    /**
     * 生成持久化房间ID
     */
    private Long nextRoomId() {
        String key = RedisKeyConstants.PAIJIU_ROOM_ID_INCR;

        Long roomId = redisUtil.incr(key, 1);

        // 第一次初始化房间号
        if (roomId != null && roomId == 1L) {
            redisUtil.set(key, 1000L);
            roomId = redisUtil.incr(key, 1);
        }

        return roomId;
    }

    public PaiJiuRoom findWaitRoom(){
        for(PaiJiuRoom room : roomMap.values()){
            Integer emptySeatId = room.findEmptySeat();
            // 等待状态且有空座位
            if(emptySeatId != null && room.getState() == RoomState.WAIT){
                return room;
            }
        }
        return null;
    }

}

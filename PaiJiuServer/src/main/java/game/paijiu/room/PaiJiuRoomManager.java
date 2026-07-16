package game.paijiu.room;


import game.common.constant.RedisKeyConstants;
import game.common.constant.RoomState;
import game.common.constant.RoomType;
import game.common.entity.RoomDTO;
import game.common.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class PaiJiuRoomManager {
    private static final long ROOM_INVITE_EXPIRE_SECONDS = 6 * 60 * 60;
    private static final String ROOM_INVITE_LETTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int ROOM_INVITE_LENGTH = 6;

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
    public PaiJiuRoom createRoom(RoomType roomType, String gatewayId, long maxRoundId) {
        for (int i = 0; i < 1000; i++) {
            Long roomId = nextRoomId();
            if (roomId == null) {
                continue;
            }

            if (roomMap.containsKey(roomId) || redisUtil.hasKey(RedisKeyConstants.roomSnapshot(roomId))) {
                log.warn("生成房间号冲突，重新生成 roomId:{} roomType:{}", roomId, roomType);
                continue;
            }

            PaiJiuRoom room = new PaiJiuRoom(roomId, roomType, 8, 10, maxRoundId);
            PaiJiuRoom oldRoom = roomMap.putIfAbsent(roomId, room);
            if (oldRoom != null) {
                log.warn("内存房间号冲突，重新生成 roomId:{} roomType:{}", roomId, roomType);
                continue;
            }

            save(room);
            log.info("创建房间成功 roomId:{} roomType:{}", roomId, roomType);
            room.init(gatewayId, this);
            return room;
        }

        throw new IllegalStateException("创建房间失败，无法生成唯一房间号");
    }

    /**
     * 获取已经存在的房间
     */
    public PaiJiuRoom getRoom(Long roomId, String gatewayId) {
        if (roomId == null) {
            return null;
        }

        PaiJiuRoom room = roomMap.get(roomId);
        if (room != null) {
            room.init(gatewayId, this);
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

            paiJiuRoom.init(gatewayId, this);
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
        PaiJiuRoom room = roomMap.remove(roomId);
        redisUtil.del(RedisKeyConstants.roomSnapshot(roomId));
        removeRoomInvite(roomId);
        room.getPlayers().keySet().forEach(userId -> removeUserRoom(userId, roomId));
        room.destroy();
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

    public PaiJiuRoom get(Long roomId, String gatewayId) {
        if (roomId == null) {
            return null;
        }

        PaiJiuRoom room = roomMap.get(roomId);
        if (room != null) {
            room.init(gatewayId, this);
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
            paiJiuRoom.init(gatewayId, this);
            return paiJiuRoom;
        }
    }

    public Long getRoomIdByUserId(Long userId) {
        if (playerRoomMap.containsKey(userId)) {
            return playerRoomMap.get(userId);
        }
        Long roomId = redisUtil.get(RedisKeyConstants.userRoom(userId), Long.class);
        if (roomId != null) {
            playerRoomMap.put(userId, roomId);
            return roomId;
        }
        return null;
    }

    public String createInvite(Long roomId) {
        if (roomId == null || !existsRoom(roomId)) {
            return null;
        }

        String roomInviteKey = RedisKeyConstants.roomInviteByRoom(roomId);
        String oldInvite = redisUtil.get(roomInviteKey, String.class);
        if (oldInvite != null && !oldInvite.trim().isEmpty()) {
            Long oldRoomId = redisUtil.get(RedisKeyConstants.roomInvite(oldInvite), Long.class);
            if (roomId.equals(oldRoomId)) {
                return oldInvite;
            }
        }

        for (int i = 0; i < 20; i++) {
            String invite = nextInviteCode();
            if (redisUtil.hasKey(RedisKeyConstants.roomInvite(invite))) {
                continue;
            }

            redisUtil.set(RedisKeyConstants.roomInvite(invite), roomId, ROOM_INVITE_EXPIRE_SECONDS);
            redisUtil.set(roomInviteKey, invite, ROOM_INVITE_EXPIRE_SECONDS);
            return invite;
        }

        log.warn("生成房间邀请码失败 roomId:{}", roomId);
        return null;
    }

    public Long getRoomIdByInvite(String invite) {
        if (invite == null || invite.trim().isEmpty()) {
            return null;
        }

        return redisUtil.get(RedisKeyConstants.roomInvite(invite.trim().toUpperCase()), Long.class);
    }

    private boolean existsRoom(Long roomId) {
        return roomMap.containsKey(roomId) || redisUtil.hasKey(RedisKeyConstants.roomSnapshot(roomId));
    }

    private void removeRoomInvite(Long roomId) {
        if (roomId == null) {
            return;
        }

        String roomInviteKey = RedisKeyConstants.roomInviteByRoom(roomId);
        String invite = redisUtil.get(roomInviteKey, String.class);
        if (invite != null && !invite.trim().isEmpty()) {
            redisUtil.del(RedisKeyConstants.roomInvite(invite));
        }
        redisUtil.del(roomInviteKey);
    }

    private String nextInviteCode() {
        StringBuilder builder = new StringBuilder(ROOM_INVITE_LENGTH);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < ROOM_INVITE_LENGTH; i++) {
            int index = random.nextInt(ROOM_INVITE_LETTERS.length());
            builder.append(ROOM_INVITE_LETTERS.charAt(index));
        }
        return builder.toString();
    }

    /**
     * 生成持久化房间ID
     */
    private Long nextRoomId() {
        String key = RedisKeyConstants.PAIJIU_ROOM_ID_INCR;

        Long roomId = redisUtil.incr(key, 1);

        // 第一次初始化房间号
        if (roomId != null && roomId == 1L) {
            redisUtil.set(key, 100000L);
            roomId = redisUtil.incr(key, 1);
        }

        return roomId;
    }

    public PaiJiuRoom findWaitRoom(){
        for(PaiJiuRoom room : roomMap.values()){
            if (room.getRoomType() != RoomType.FREE_MATCH) {
                continue;
            }

            Integer emptySeatId = room.findEmptySeat();
            // 等待状态且有空座位
            if(emptySeatId != null){
                if(room.getState() == RoomState.WAIT || room.getState() == RoomState.READY){
                    return room;
                }
            }
        }
        return null;
    }

}

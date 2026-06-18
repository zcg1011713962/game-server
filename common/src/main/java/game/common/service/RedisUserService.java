package game.common.service;

import cn.hutool.core.bean.BeanUtil;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RedisUserService {

    @Autowired
    private RedisUtil redisUtil;

    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60;

    private static final DefaultRedisScript<Long> CHANGE_ASSET_SCRIPT;

    static {
        CHANGE_ASSET_SCRIPT = new DefaultRedisScript<>();
        CHANGE_ASSET_SCRIPT.setResultType(Long.class);
        CHANGE_ASSET_SCRIPT.setScriptText(
                "local current = tonumber(redis.call('HGET', KEYS[1], ARGV[1]) or 0) "
                        + "local delta = tonumber(ARGV[2]) "
                        + "local after = current + delta "
                        + "if after < 0 then "
                        + " return -1 "
                        + "end "
                        + "redis.call('HINCRBY', KEYS[1], ARGV[1], delta) "
                        + "return after "
        );
    }

    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        Map<Object, Object> map = redisUtil.assetHMGet(RedisKeyConstants.player(userId));
        if(map.isEmpty()){
            return null;
        }
        return BeanUtil.fillBeanWithMap(map, new User(), true);
    }

    public void saveUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        redisUtil.assetHMSet(
                RedisKeyConstants.player(user.getId()),
                BeanUtil.beanToMap(user, false, true),
                EXPIRE_TIME
        );
    }

    public Long getField(Long userId, String field) {
        return redisUtil.assetHGetLong(
                RedisKeyConstants.player(userId),
                field
        );
    }

    public Long getGold(Long userId) {
        return getField(userId, "gold");
    }

    public Long changeGold(Long userId, long change) {
        return redisUtil.changeAsset(RedisKeyConstants.player(userId), "gold", change);
    }

    public Long getRoomCard(Long userId) {
        return getField(userId, "roomCard");
    }

    public Long changeRoomCard(Long userId, long change) {
        return redisUtil.changeAsset(RedisKeyConstants.player(userId), "roomCard", change);
    }


}

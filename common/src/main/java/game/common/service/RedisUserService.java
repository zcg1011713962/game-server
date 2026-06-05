package game.common.service;

import cn.hutool.core.bean.BeanUtil;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisUserService {

    @Autowired
    private RedisUtil redisUtil;

    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60;

    public User getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        return redisUtil.hmget(
                RedisKeyConstants.player(userId),
                User.class
        );
    }

    public void saveUser(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        redisUtil.hmset(
                RedisKeyConstants.player(user.getId()),
                BeanUtil.beanToMap(user, false, true),
                EXPIRE_TIME
        );
    }

    public Object getField(Long userId, String field) {
        return redisUtil.hmget(
                RedisKeyConstants.player(userId),
                field
        );
    }

    public Long hincr(Long userId, String field, long delta) {
        return redisUtil.hincr(
                RedisKeyConstants.player(userId),
                field,
                delta
        );
    }

    public Long getGold(Long userId) {
        Object value = getField(userId, "gold");
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    public Long changeGold(Long userId, long delta) {
        return hincr(userId, "gold", delta);
    }

    public Long getRoomCard(Long userId) {
        Object value = getField(userId, "roomCard");
        return value == null ? 0L : Long.parseLong(value.toString());
    }

    public Long changeRoomCard(Long userId, long delta) {
        return hincr(userId, "roomCard", delta);
    }
}

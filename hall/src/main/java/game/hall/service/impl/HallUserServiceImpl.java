package game.hall.service.impl;

import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.service.UserService;
import game.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HallUserServiceImpl implements UserService {
    @Autowired
    RedisUtil redisUtil;
    @Override
    public User getUserById(Long userId) {
        return redisUtil.get(RedisKeyConstants.player(userId));
    }
}

package game.hall.server;

import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.service.UserService;
import game.hall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HallUserService implements UserService {
    @Autowired
    RedisUtil redisUtil;
    @Override
    public User getUserById(Long userId) {
        return redisUtil.get(RedisKeyConstants.player(userId));
    }
}

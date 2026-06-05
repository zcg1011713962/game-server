package game.paijiu.service;

import cn.hutool.core.bean.BeanUtil;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.service.UserService;
import game.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GameUserService implements UserService {
    @Autowired
    RedisUtil redisUtil;

    @Override
    public User getUserById(Long userId) {
        Map<Object, Object> map =  redisUtil.hmget(RedisKeyConstants.player(userId));
        return BeanUtil.toBean(map, User.class);
    }


}

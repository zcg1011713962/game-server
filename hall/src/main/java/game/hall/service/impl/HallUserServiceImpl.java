package game.hall.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import game.common.constant.PropCodeEnum;
import game.common.entity.User;
import game.common.service.RedisUserService;
import game.common.service.UserService;
import game.hall.mybatis.domain.DbUser;
import game.hall.mybatis.mapper.DbUserMapper;
import game.hall.mybatis.service.DbUserService;
import game.hall.service.UserBagService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class HallUserServiceImpl implements UserService {

    @Autowired
    private DbUserService dbUserService;

    @Autowired
    private UserBagService userBagService;

    @Autowired
    private RedisUserService redisUserService;
    @Autowired
    private DbUserMapper dbUserMapper;

    @Override
    public User getUserById(Long userId) {
        User user = redisUserService.getUserById(userId);
        long roomCard = userBagService.getPropCount(
                userId,
                PropCodeEnum.ROOM_CARD.getCode()
        );
        if (user != null) {
            user.setRoomCard(roomCard);
            return user;
        }

        DbUser dbUser = dbUserService.getById(userId);

        if (dbUser == null) {
            return null;
        }

        user = convertToUser(dbUser);
        user.setRoomCard(roomCard);
        redisUserService.saveUser(user);
        return user;
    }

    @Override
    public void refreshUserCache(Long userId) {
        DbUser dbUser = dbUserService.getById(userId);

        if (dbUser == null) {
            return;
        }

        User user = convertToUser(dbUser);

        long roomCard = userBagService.getPropCount(
                userId,
                PropCodeEnum.ROOM_CARD.getCode()
        );

        user.setRoomCard(roomCard);

        redisUserService.saveUser(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long changeGold(Long userId, long change) {
        if (userId == null || change == 0) {
            throw new RuntimeException("金币变更参数错误");
        }

        DbUser dbUser = dbUserService.getById(userId);

        if (dbUser == null) {
            throw new RuntimeException("用户不存在");
        }

        long oldGold = dbUser.getGold() == null ? 0L : dbUser.getGold();
        long newGold = oldGold + change;

        if (newGold < 0) {
            throw new RuntimeException("金币不足");
        }

        dbUser.setGold(newGold);

        boolean ok = dbUserService.updateById(dbUser);

        if (!ok) {
            throw new RuntimeException("更新金币失败");
        }
        refreshUserCache(userId);
        return newGold;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long changeRoomCard(Long userId, long change) {
        if (userId == null || change == 0) {
            throw new RuntimeException("房卡变更参数错误");
        }

        long oldCount = userBagService.getPropCount(userId, PropCodeEnum.ROOM_CARD.getCode());
        long newCount = oldCount + change;

        if (newCount < 0) {
            throw new RuntimeException("房卡不足");
        }
        userBagService.changeProp(userId, PropCodeEnum.ROOM_CARD.getCode(), change);
        refreshUserCache(userId);
        return newCount;
    }

    private User convertToUser(DbUser dbUser) {
        User user = new User();
        BeanUtils.copyProperties(dbUser, user);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByRedis(Long userId) {
        Long gold = redisUserService.getGold(userId);
        DbUser dbUser = new DbUser();
        dbUser.setId(userId);
        dbUser.setGold(gold == null ? 0L : gold);
        dbUser.setUpdateTime(new Date());
        dbUserMapper.updateById(dbUser);
    }
}
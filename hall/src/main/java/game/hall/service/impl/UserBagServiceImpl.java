package game.hall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import game.common.constant.AssetType;
import game.common.constant.PropCodeEnum;
import game.common.service.RedisUserService;
import game.hall.mybatis.domain.DbUserBag;
import game.hall.mybatis.mapper.DbUserBagMapper;
import game.hall.service.UserBagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service
public class UserBagServiceImpl implements UserBagService {

    @Autowired
    private DbUserBagMapper userBagMapper;
    @Autowired
    RedisUserService redisUserService;

    @Transactional(rollbackFor = Exception.class)
    public long changeProp(Long userId, String propCode, long changeCount) {

        if (userId == null || StringUtils.isBlank(propCode) || changeCount == 0) {
            throw new RuntimeException("道具变更参数错误");
        }
        DbUserBag bag = userBagMapper.selectOne(
                new LambdaQueryWrapper<DbUserBag>()
                        .eq(DbUserBag::getUserId, userId)
                        .eq(DbUserBag::getPropCode, propCode)
        );
        Date now = new Date();
        // 不存在
        if (bag == null) {
            if (changeCount < 0) {
                throw new RuntimeException("道具数量不足");
            }
            bag = new DbUserBag();
            bag.setUserId(userId);
            bag.setPropCode(propCode);
            bag.setPropCount(changeCount);
            bag.setCreateTime(now);
            bag.setUpdateTime(now);
            redisUserService.changeRoomCard(userId, changeCount);
            userBagMapper.insert(bag);
            return changeCount;
        }

        long newCount = bag.getPropCount() + changeCount;
        if (newCount < 0) {
            throw new RuntimeException("道具数量不足");
        }
        bag.setPropCount(newCount);
        bag.setUpdateTime(now);
        redisUserService.changeRoomCard(userId, changeCount);
        userBagMapper.updateById(bag);
        return newCount;
    }

    /**
     * 查询单个道具数量
     */
    public long getPropCount(Long userId, String propCode) {

        DbUserBag bag = userBagMapper.selectOne(
                new LambdaQueryWrapper<DbUserBag>()
                        .eq(DbUserBag::getUserId, userId)
                        .eq(DbUserBag::getPropCode, propCode)
        );
        return bag == null ? 0 : bag.getPropCount();
    }

    /**
     * 查询用户背包
     */
    public List<DbUserBag> listUserBag(Long userId) {

        return userBagMapper.selectList(
                new LambdaQueryWrapper<DbUserBag>()
                        .eq(DbUserBag::getUserId, userId)
                        .gt(DbUserBag::getPropCount, 0)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateByRedis(Long userId) {
        Long roomCard = redisUserService.getField(userId, AssetType.ROOM_CARD.getField());
        UpdateWrapper<DbUserBag> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("prop_code", PropCodeEnum.ROOM_CARD.getCode())
                .set("prop_count", roomCard == null ? 0L : roomCard)
                .set("update_time", new Date());
        int rows = userBagMapper.update(updateWrapper);
    }
}

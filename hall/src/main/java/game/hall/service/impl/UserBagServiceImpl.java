package game.hall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import game.hall.mybatis.domain.DbUserBag;
import game.hall.mybatis.mapper.DbUserBagMapper;
import game.hall.service.UserBagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service
public class UserBagServiceImpl implements UserBagService {

    @Autowired
    private DbUserBagMapper userBagMapper;

    /**
     * 增加道具
     */
    @Transactional(rollbackFor = Exception.class)
    public void addProp(Long userId, String propCode, long count) {
        if (userId == null || propCode == null || count <= 0) {
            throw new RuntimeException("增加道具参数错误");
        }

        DbUserBag bag = userBagMapper.selectOne(
                new LambdaQueryWrapper<DbUserBag>()
                        .eq(DbUserBag::getUserId, userId)
                        .eq(DbUserBag::getPropCode, propCode)
        );
        Date now = new Date();
        if (bag == null) {
            bag = new DbUserBag();
            bag.setUserId(userId);
            bag.setPropCode(propCode);
            bag.setPropCount(count);
            bag.setCreateTime(now);
            bag.setUpdateTime(now);

            userBagMapper.insert(bag);
            return;
        }
        bag.setPropCount(bag.getPropCount() + count);
        bag.setUpdateTime(now);
        userBagMapper.updateById(bag);
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

    @Transactional(rollbackFor = Exception.class)
    public void reduceProp(Long userId, String propCode, long count) {
        if (userId == null || propCode == null || count <= 0) {
            throw new RuntimeException("扣除道具参数错误");
        }

        DbUserBag bag = userBagMapper.selectOne(
                new LambdaQueryWrapper<DbUserBag>()
                        .eq(DbUserBag::getUserId, userId)
                        .eq(DbUserBag::getPropCode, propCode)
        );

        if (bag == null || bag.getPropCount() < count) {
            throw new RuntimeException("道具数量不足");
        }

        bag.setPropCount(bag.getPropCount() - count);
        userBagMapper.updateById(bag);
    }
}

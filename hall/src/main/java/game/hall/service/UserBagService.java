package game.hall.service;

import game.hall.mybatis.domain.DbUserBag;

import java.util.List;

public interface UserBagService {
    long changeProp(Long userId, String propCode, long changeCount);

    long getPropCount(Long userId, String propCode);

    List<DbUserBag> listUserBag(Long userId);

    void updateByRedis(Long userId);
}

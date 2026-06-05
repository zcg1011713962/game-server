package game.hall.service;

import game.hall.mybatis.domain.DbUserBag;

import java.util.List;

public interface UserBagService {
    void addProp(Long userId, String propCode, long count);

    long getPropCount(Long userId, String propCode);

    List<DbUserBag> listUserBag(Long userId);

    void reduceProp(Long userId, String propCode, long count);
}

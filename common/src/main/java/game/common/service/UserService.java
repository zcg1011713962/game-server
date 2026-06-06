package game.common.service;

import game.common.entity.User;

public interface UserService {
    User getUserById(Long userId);

    Long changeRoomCard(Long userId, long change);

    void refreshUserCache(Long userId);

    Long changeGold(Long userId, long change);

    void updateByRedis(Long userId);
}

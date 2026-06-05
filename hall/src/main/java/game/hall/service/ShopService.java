package game.hall.service;

import game.common.entity.User;

public interface ShopService {

    User buyProduct(Long userId, Long productId);

}

package game.hall.service.impl;

import game.common.constant.PropCodeEnum;
import game.common.entity.User;
import game.common.service.RedisUserService;
import game.common.service.UserService;
import game.hall.exception.HallException;
import game.hall.mybatis.domain.DbShopConfig;
import game.hall.mybatis.mapper.DbShopConfigMapper;
import game.hall.service.ShopService;
import game.hall.service.UserBagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private DbShopConfigMapper shopConfigMapper;

    @Autowired
    private UserBagService userBagService;
    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User buyProduct(Long userId, Long productId) {
        if (productId == null) {
            throw new HallException("商品ID不能为空");
        }
        DbShopConfig config = shopConfigMapper.selectById(productId);

        if (config == null) {
            throw new HallException("商品不存在");
        }

        if (!Integer.valueOf(1).equals(config.getIsEnable())) {
            throw new HallException("商品已下架");
        }

        long totalCount = config.getItemCount() + config.getGiftCount();

        if (totalCount <= 0) {
            throw new HallException("商品数量错误");
        }

        // 当前先按 productType 发放
        if (Integer.valueOf(1).equals(config.getProductType())) {

            userBagService.addProp(
                    userId,
                    PropCodeEnum.ROOM_CARD.getCode(),
                    totalCount
            );

        } else if (Integer.valueOf(2).equals(config.getProductType())) {

            userBagService.addProp(
                    userId,
                    PropCodeEnum.GOLD.getCode(),
                    totalCount
            );

        } else if (Integer.valueOf(3).equals(config.getProductType())) {

            userBagService.addProp(
                    userId,
                    PropCodeEnum.DIAMOND.getCode(),
                    totalCount
            );

        } else {
            throw new HallException("暂不支持该商品类型");
        }
        log.info("购买商品:{} 成功,userId:{}", config.getProductName(), userId);
        return userService.getUserById(userId);
    }
}

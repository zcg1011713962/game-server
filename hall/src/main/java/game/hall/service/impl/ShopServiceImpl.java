package game.hall.service.impl;

import game.common.constant.PropCodeEnum;
import game.hall.mybatis.domain.DbShopConfig;
import game.hall.mybatis.mapper.DbShopConfigMapper;
import game.hall.service.ShopService;
import game.hall.service.UserBagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private DbShopConfigMapper shopConfigMapper;

    @Autowired
    private UserBagService userBagService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyProduct(Long userId, Long productId) {

        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        if (productId == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        DbShopConfig config = shopConfigMapper.selectById(productId);

        if (config == null) {
            throw new RuntimeException("商品不存在");
        }

        if (!Integer.valueOf(1).equals(config.getIsEnable())) {
            throw new RuntimeException("商品已下架");
        }

        long totalCount =
                config.getItemCount() + config.getGiftCount();

        if (totalCount <= 0) {
            throw new RuntimeException("商品数量错误");
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
            throw new RuntimeException("暂不支持该商品类型");
        }
    }
}

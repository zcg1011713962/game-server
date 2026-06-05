package game.hall.web;

import game.common.protocol.ServerMsg;
import game.hall.entity.req.BuyProductReq;
import game.hall.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @PostMapping("/buy")
    public ServerMsg buy(@RequestBody BuyProductReq req) {
        Long userId = UserContext.getUserId();
        shopService.buyProduct(userId, req.getProductId());
        return ServerMsg.ok();
    }
}

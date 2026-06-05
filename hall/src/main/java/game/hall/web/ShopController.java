package game.hall.web;

import game.common.constant.ErrorCode;
import game.common.context.UserContext;
import game.common.protocol.ServerMsg;
import game.hall.entity.req.BuyProductReq;
import game.hall.exception.HallException;
import game.hall.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ShopController {

    @Autowired
    private ShopService shopService;

    @PostMapping("/shop/buy")
    public ServerMsg buy(@RequestBody BuyProductReq req) {
        Long userId = UserContext.getUserId();
        if(userId == null){
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }
        try {
            return ServerMsg.ok(shopService.buyProduct(userId, req.getProductId()));
        }catch (HallException e){
            return ServerMsg.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            return ServerMsg.error(ErrorCode.SYSTEM_ERROR);
        }
    }
}

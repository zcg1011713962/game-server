package game.hall.web;


import game.common.constant.ErrorCode;
import game.common.protocol.ServerMsg;
import game.hall.entity.req.GuestLoginReq;
import game.hall.service.impl.LoginServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private LoginServiceImpl loginService;

    @PostMapping("/login/guest")
    public ServerMsg guestLogin(@RequestBody GuestLoginReq guestLoginReq) {
        try {
            return loginService.loginByGuest(guestLoginReq);
        } catch (Exception e) {
            log.error("guestLogin:{}", e.getMessage());
            return ServerMsg.error(ErrorCode.SYSTEM_ERROR);
        }
    }


}
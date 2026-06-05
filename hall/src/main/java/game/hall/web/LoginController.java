package game.hall.web;


import game.common.protocol.ServerMsg;
import game.hall.entity.req.GuestLoginReq;
import game.hall.service.impl.LoginServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private LoginServiceImpl loginService;

    @PostMapping("/login/guest")
    public ServerMsg guestLogin(@RequestBody GuestLoginReq guestLoginReq) {
        return loginService.loginByGuest(guestLoginReq);
    }


}
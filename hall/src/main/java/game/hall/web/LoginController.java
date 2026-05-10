package game.hall.web;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import game.common.constant.ErrorCode;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.common.util.JwtUtil;
import game.hall.domain.DbUser;
import game.hall.entity.req.GuestLoginReq;
import game.hall.entity.res.LoginResp;
import game.hall.server.LoginService;
import game.hall.service.DbUserService;
import game.hall.util.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping("/login/guest")
    public ServerMsg guestLogin(@RequestBody GuestLoginReq guestLoginReq) {
        return ServerMsg.ok(loginService.loginByGuest(guestLoginReq));
    }


}
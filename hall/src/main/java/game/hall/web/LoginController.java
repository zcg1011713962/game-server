package game.hall.web;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.common.util.JwtUtil;
import game.hall.domain.DbUser;
import game.hall.entity.req.LoginReq;
import game.hall.entity.res.LoginResp;
import game.hall.service.DbUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LoginController {
    @Autowired
    private DbUserService dbUserService;

    @PostMapping("/login")
    public ServerMsg login(@RequestBody LoginReq req) {
        if (req.getUsername() == null || req.getPwd() == null) {
            return ServerMsg.error(Cmd.LOGIN_RESULT.value(), 0, 1001, "参数错误");
        }
        QueryWrapper<DbUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", req.getUsername());
        DbUser dbUser = dbUserService.getBaseMapper().selectOne(queryWrapper);
        if (dbUser == null || !(req.getUsername().equals(dbUser.getUsername()) || req.getPwd().equals(dbUser.getPwd()))) {
            return ServerMsg.error(Cmd.LOGIN_RESULT.value(), 0, 2001, "账号或密码错误");
        }
        String token = JwtUtil.generateToken(dbUser.getId());
        return ServerMsg.ok(Cmd.LOGIN_RESULT.value(), 0, LoginResp.builder().userId(dbUser.getId()).token(token).build());
    }
}
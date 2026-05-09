package game.hall.web;


import ch.qos.logback.core.testUtil.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import game.common.constant.ErrorCode;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.protocol.Cmd;
import game.common.protocol.ServerMsg;
import game.common.util.JwtUtil;
import game.hall.domain.DbUser;
import game.hall.entity.req.LoginReq;
import game.hall.entity.res.LoginResp;
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
    private DbUserService dbUserService;

    @Autowired
    private RedisUtil redisUtil;

    @PostMapping("/login")
    public ServerMsg login(@RequestBody LoginReq req) {

        if (req.getUsername() == null || req.getPwd() == null) {
            return ServerMsg.error(Cmd.LOGIN_RESULT.value(), 0, ErrorCode.PARAM_ERROR);
        }

        QueryWrapper<DbUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", req.getUsername());

        DbUser dbUser = dbUserService.getBaseMapper().selectOne(queryWrapper);

        // 注意：这里应该是用户名不等 或 密码不等
        if (dbUser == null
                || !req.getUsername().equals(dbUser.getUsername())
                || !req.getPwd().equals(dbUser.getPwd())) {
            return ServerMsg.error(Cmd.LOGIN_RESULT.value(), 0, ErrorCode.LOGIN_ERROR);
        }
        String token = JwtUtil.generateToken(dbUser.getId());

        User user = new User();
        BeanUtils.copyProperties(dbUser, user);
        // 登录成功后缓存玩家信息
        redisUtil.set(RedisKeyConstants.player(dbUser.getId()), user, 7 * 24 * 60 * 60);

        return ServerMsg.ok(
                Cmd.LOGIN_RESULT.value(),
                0,
                LoginResp.builder()
                        .userId(dbUser.getId())
                        .token(token)
                        .build(),
                0,
                "ok"
        );
    }


    @PostMapping("/login/guest")
    public ServerMsg guestLogin() {
        String userName;
        String nickName;
        long id;
        while (true) {
            id = ThreadLocalRandom.current().nextLong(50000, 100000);
            userName = "guest" + id;
            nickName = "player" + id;
            QueryWrapper<DbUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", userName);

            DbUser dbUser1 = dbUserService.getBaseMapper().selectOne(queryWrapper);
            dbUserService.getOne(queryWrapper);
            if(dbUser1 == null || dbUser1.getId() == null){
                break;
            }
        }
        long avatarId = ThreadLocalRandom.current().nextLong(0, 5);
        DbUser dbUser = new DbUser();
        dbUser.setUsername(userName);
        dbUser.setPwd("12345678");
        dbUser.setAvatar(String.valueOf(avatarId));
        dbUser.setGold(0L);
        dbUser.setNickname(nickName);
        dbUser.setId(id);
        int ret = dbUserService.getBaseMapper().insert(dbUser);
        if (ret > 0) {
            return ServerMsg.ok(dbUser);
        }
        return ServerMsg.error(null, 0, ErrorCode.CREATE_USER_ERROR);
    }


}
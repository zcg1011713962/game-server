package game.hall.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.service.UserService;
import game.common.util.JwtUtil;
import game.hall.domain.DbUser;
import game.hall.entity.req.GuestLoginReq;
import game.hall.entity.res.LoginResp;
import game.hall.service.DbUserService;
import game.hall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class LoginService {
    @Autowired
    private DbUserService dbUserService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    UserService userService;

    public LoginResp loginByGuest(GuestLoginReq guestLoginReq){
        if(StringUtils.isEmpty(guestLoginReq.getToken())){
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
                String token = JwtUtil.generateToken(dbUser.getId());
                User user = new User();
                BeanUtils.copyProperties(dbUser, user);

                redisUtil.set(RedisKeyConstants.token(token), dbUser.getId(), 7 * 24 * 60 * 60);
                // 登录成功后缓存玩家信息
                redisUtil.set(RedisKeyConstants.player(dbUser.getId()), user, 7 * 24 * 60 * 60);
                return LoginResp.builder()
                        .userId(id)
                        .nickname(nickName)
                        .avatar(String.valueOf(avatarId))
                        .gold(0L)
                        .token(token)
                        .build();
            }
        }else {
            Long userId = redisUtil.get(RedisKeyConstants.token(guestLoginReq.getToken()), Long.class);
            User user = userService.getUserById(userId);
            return LoginResp.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .avatar(String.valueOf(user.getAvatar()))
                    .gold(0L)
                    .token(guestLoginReq.getToken())
                    .build();
        }
        return LoginResp.builder().build();
    }

}

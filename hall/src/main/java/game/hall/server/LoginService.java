package game.hall.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import game.common.constant.ErrorCode;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.protocol.ServerMsg;
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

    private static final long expireTime = 7 * 24 * 60 * 60;

    public ServerMsg loginByGuest(GuestLoginReq guestLoginReq){
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

            DbUser dbUser = getDbUser(id, userName, "12345678", String.valueOf(avatarId), 1000L, 0L, nickName);
            int ret = dbUserService.getBaseMapper().insert(dbUser);
            if (ret > 0) {
                String token = JwtUtil.generateToken(dbUser.getId());
                User user = new User();
                BeanUtils.copyProperties(dbUser, user);

                redisUtil.set(RedisKeyConstants.token(token), dbUser.getId(), expireTime);
                // 登录成功后缓存玩家信息
                redisUtil.set(RedisKeyConstants.player(dbUser.getId()), user, expireTime);
                return ServerMsg.ok(LoginResp.builder()
                        .userId(id)
                        .nickname(nickName)
                        .avatar(String.valueOf(avatarId))
                        .gold(dbUser.getGold())
                        .diamond(dbUser.getDiamond())
                        .token(token)
                        .build());
            }else {
                return ServerMsg.error(null, 0, ErrorCode.CREATE_USER_ERROR);
            }
        }else {
            Long userId = redisUtil.get(RedisKeyConstants.token(guestLoginReq.getToken()), Long.class);
            if(userId == null){
                return ServerMsg.error(null, 0, ErrorCode.TOKEN_INVALID);
            }
            User user = userService.getUserById(userId);
            DbUser dbUser = dbUserService.getById(userId);
            if(dbUser != null){
                BeanUtils.copyProperties(dbUser, user);
                redisUtil.set(RedisKeyConstants.token(guestLoginReq.getToken()), dbUser.getId(), expireTime);
            }
            return ServerMsg.ok(LoginResp.builder()
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .avatar(String.valueOf(user.getAvatar()))
                    .gold(user.getGold())
                    .diamond(user.getDiamond())
                    .token(guestLoginReq.getToken())
                    .build());
        }
    }

    public DbUser getDbUser(Long id, String userName, String pwd, String avatar, Long gold, Long diamond, String nickName) {
        DbUser dbUser = new DbUser();
        dbUser.setUsername(userName);
        dbUser.setPwd(pwd);
        dbUser.setAvatar(avatar);
        dbUser.setGold(gold);
        dbUser.setNickname(nickName);
        dbUser.setDiamond(diamond);
        dbUser.setId(id);
        return dbUser;
    }

}

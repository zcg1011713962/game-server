package game.hall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import game.common.constant.ErrorCode;
import game.common.constant.RedisKeyConstants;
import game.common.entity.User;
import game.common.protocol.ServerMsg;
import game.common.service.UserService;
import game.common.util.JwtUtil;
import game.common.util.RedisUtil;
import game.hall.entity.req.GuestLoginReq;
import game.hall.entity.res.LoginResp;
import game.hall.mybatis.domain.DbUser;
import game.hall.mybatis.service.DbUserService;
import game.hall.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class LoginServiceImpl implements LoginService {
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

            DbUser dbUser = getDbUser(id, userName, "12345678", String.valueOf(avatarId), 1000L, nickName);
            int ret = dbUserService.getBaseMapper().insert(dbUser);
            if (ret > 0) {
                String token = JwtUtil.generateToken(dbUser.getId());
                redisUtil.hmset(RedisKeyConstants.player(dbUser.getId()), BeanUtil.beanToMap(dbUser), expireTime);
                return ServerMsg.ok(LoginResp.builder()
                        .userId(id)
                        .nickname(nickName)
                        .avatar(String.valueOf(avatarId))
                        .gold(dbUser.getGold())
                        .token(token)
                        .build());
            }else {
                return ServerMsg.error(null, 0, ErrorCode.CREATE_USER_ERROR);
            }
        }else {
            Long userId = JwtUtil.getUserId(guestLoginReq.getToken());
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
                    .token(guestLoginReq.getToken())
                    .build());
        }
    }

    public DbUser getDbUser(Long id, String userName, String pwd, String avatar, Long gold, String nickName) {
        DbUser dbUser = new DbUser();
        dbUser.setUsername(userName);
        dbUser.setPwd(pwd);
        dbUser.setAvatar(avatar);
        dbUser.setGold(gold);
        dbUser.setNickname(nickName);
        dbUser.setId(id);
        return dbUser;
    }

}

package game.hall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import game.common.constant.ErrorCode;
import game.common.constant.PropCodeEnum;
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
import game.hall.service.UserBagService;
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
    private UserService userService;
    @Autowired
    private UserBagService userBagService;

    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60;

    @Override
    public ServerMsg loginByGuest(GuestLoginReq req) {
        if (StringUtils.isBlank(req.getToken())) {
            return createGuestUser();
        }
        return loginByToken(req.getToken());
    }

    private ServerMsg createGuestUser() {
        DbUser dbUser = buildGuestUser();
        int ret = dbUserService.getBaseMapper().insert(dbUser);
        if (ret <= 0) {
            return ServerMsg.error(null, 0, ErrorCode.CREATE_USER_ERROR);
        }
        cacheUser(dbUser);
        return buildLoginResp(
                dbUser.getId(),
                dbUser.getNickname(),
                String.valueOf(dbUser.getAvatar()),
                dbUser.getGold(),
                JwtUtil.generateToken(dbUser.getId())
        );
    }

    private ServerMsg loginByToken(String token) {
        Long userId = JwtUtil.getUserId(token);
        if (userId == null) {
            return ServerMsg.error(null, 0, ErrorCode.TOKEN_INVALID);
        }
        User user = userService.getUserById(userId);
        if (user != null) {
            return buildLoginResp(
                    user.getId(),
                    user.getNickname(),
                    String.valueOf(user.getAvatar()),
                    user.getGold(),
                    token
            );
        }

        DbUser dbUser = dbUserService.getBaseMapper().selectById(userId);

        if (dbUser == null) {
            return ServerMsg.error(null, 0, ErrorCode.TOKEN_INVALID);
        }

        cacheUser(dbUser);

        return buildLoginResp(
                dbUser.getId(),
                dbUser.getNickname(),
                String.valueOf(dbUser.getAvatar()),
                dbUser.getGold(),
                token
        );
    }

    private ServerMsg buildLoginResp(Long userId,
                                     String nickname,
                                     String avatar,
                                     Long gold,
                                     String token) {

        long roomCard = userBagService.getPropCount(
                userId,
                PropCodeEnum.ROOM_CARD.getCode()
        );

        return ServerMsg.ok(LoginResp.builder()
                .userId(userId)
                .nickname(nickname)
                .avatar(avatar)
                .gold(gold)
                .token(token)
                .roomCard(roomCard)
                .build());
    }

    private void cacheUser(DbUser dbUser) {
        redisUtil.hmset(
                RedisKeyConstants.player(dbUser.getId()),
                BeanUtil.beanToMap(dbUser, false, true),
                EXPIRE_TIME
        );
    }

    private DbUser buildGuestUser() {
        while (true) {
            long id = ThreadLocalRandom.current().nextLong(50000, 100000);
            String username = "guest" + id;

            QueryWrapper<DbUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", username);

            DbUser exist = dbUserService.getBaseMapper().selectOne(queryWrapper);

            if (exist != null) {
                continue;
            }

            long avatarId = ThreadLocalRandom.current().nextLong(0, 5);

            return getDbUser(
                    id,
                    username,
                    "12345678",
                    String.valueOf(avatarId),
                    1000L,
                    "player" + id
            );
        }
    }

    private DbUser getDbUser(Long id,
                             String username,
                             String pwd,
                             String avatar,
                             Long gold,
                             String nickname) {

        DbUser dbUser = new DbUser();
        dbUser.setId(id);
        dbUser.setUsername(username);
        dbUser.setPwd(pwd);
        dbUser.setAvatar(avatar);
        dbUser.setGold(gold);
        dbUser.setNickname(nickname);
        return dbUser;
    }
}

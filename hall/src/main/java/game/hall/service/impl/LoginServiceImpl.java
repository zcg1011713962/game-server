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

    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60;
    private static final String GUEST_DEVICE_USERNAME_PREFIX = "guest_device_";
    private static final int MAX_DEVICE_ID_LENGTH = 40;

    @Override
    public ServerMsg loginByGuest(GuestLoginReq req) {
        String token = req == null ? null : req.getToken();
        String deviceId = req == null ? null : normalizeDeviceId(req.getDeviceId());

        DbUser deviceUser = getGuestUserByDeviceId(deviceId);
        if (deviceUser != null) {
            cacheUser(deviceUser);
            return buildLoginResp(deviceUser);
        }

        if (StringUtils.isNotBlank(token)) {
            ServerMsg tokenLoginResp = loginByToken(token, deviceId);
            if (tokenLoginResp.getCode() == ErrorCode.SUCCESS.code()
                    || StringUtils.isBlank(deviceId)) {
                return tokenLoginResp;
            }
        }

        return createGuestUser(deviceId);
    }

    private ServerMsg createGuestUser(String deviceId) {
        DbUser dbUser = buildGuestUser(deviceId);
        DbUser exist = getUserByUsername(dbUser.getUsername());
        if (exist != null) {
            cacheUser(exist);
            return buildLoginResp(exist);
        }

        int ret = dbUserService.getBaseMapper().insert(dbUser);
        if (ret <= 0) {
            return ServerMsg.error(null, 0, ErrorCode.CREATE_USER_ERROR);
        }
        cacheUser(dbUser);
        return buildLoginResp(dbUser);
    }

    private ServerMsg loginByToken(String token, String deviceId) {
        Long userId = JwtUtil.getUserId(token);
        if (userId == null) {
            return ServerMsg.error(null, 0, ErrorCode.TOKEN_INVALID);
        }

        DbUser dbUser = dbUserService.getBaseMapper().selectById(userId);
        if (dbUser == null) {
            return ServerMsg.error(null, 0, ErrorCode.TOKEN_INVALID);
        }

        bindGuestDevice(dbUser, deviceId);
        cacheUser(dbUser);
        return buildLoginResp(dbUser);
    }

    private ServerMsg buildLoginResp(DbUser dbUser) {
        return buildLoginResp(
                dbUser.getId(),
                dbUser.getNickname(),
                String.valueOf(dbUser.getAvatar()),
                dbUser.getGold(),
                JwtUtil.generatePermanentToken(dbUser.getId())
        );
    }

    private DbUser getGuestUserByDeviceId(String deviceId) {
        String username = getGuestDeviceUsername(deviceId);
        if (StringUtils.isBlank(username)) {
            return null;
        }

        return getUserByUsername(username);
    }

    private DbUser getUserByUsername(String username) {
        QueryWrapper<DbUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return dbUserService.getBaseMapper().selectOne(queryWrapper);
    }

    private void bindGuestDevice(DbUser dbUser, String deviceId) {
        String username = getGuestDeviceUsername(deviceId);
        if (dbUser == null || StringUtils.isBlank(username) || username.equals(dbUser.getUsername())) {
            return;
        }

        DbUser exist = getUserByUsername(username);
        if (exist != null && !exist.getId().equals(dbUser.getId())) {
            return;
        }

        dbUser.setUsername(username);
        dbUserService.getBaseMapper().updateById(dbUser);
    }

    private ServerMsg buildLoginResp(Long userId,
                                     String nickname,
                                     String avatar,
                                     Long gold,
                                     String token) {

        User user = userService.getUserById(userId);

        return ServerMsg.ok(LoginResp.builder()
                .userId(userId)
                .nickname(nickname)
                .avatar(avatar)
                .gold(gold)
                .token(token)
                .roomCard(user == null ? 0 : user.getRoomCard())
                .build());
    }

    private void cacheUser(DbUser dbUser) {
        redisUtil.assetHMSet(
                RedisKeyConstants.player(dbUser.getId()),
                BeanUtil.beanToMap(dbUser, false, true),
                EXPIRE_TIME
        );
    }

    private DbUser buildGuestUser(String deviceId) {
        String deviceUsername = getGuestDeviceUsername(deviceId);

        while (true) {
            long id = ThreadLocalRandom.current().nextLong(50000, 100000);
            if (dbUserService.getBaseMapper().selectById(id) != null) {
                continue;
            }

            String username = StringUtils.isBlank(deviceUsername) ? "guest" + id : deviceUsername;
            DbUser exist = getUserByUsername(username);

            if (exist != null) {
                return exist;
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

    private String getGuestDeviceUsername(String deviceId) {
        String normalizedDeviceId = normalizeDeviceId(deviceId);
        if (StringUtils.isBlank(normalizedDeviceId)) {
            return null;
        }
        return GUEST_DEVICE_USERNAME_PREFIX + normalizedDeviceId;
    }

    private String normalizeDeviceId(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return null;
        }

        String normalized = deviceId.trim().replaceAll("[^A-Za-z0-9_]", "");
        if (normalized.length() > MAX_DEVICE_ID_LENGTH) {
            normalized = normalized.substring(0, MAX_DEVICE_ID_LENGTH);
        }
        return normalized;
    }
}

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
import game.hall.entity.req.LoginTokenReq;
import game.hall.entity.res.LoginResp;
import game.hall.mybatis.domain.DbUser;
import game.hall.mybatis.domain.DbMail;
import game.hall.mybatis.domain.DbMailAttachment;
import game.hall.mybatis.domain.DbMailUser;
import game.hall.mybatis.mapper.DbMailAttachmentMapper;
import game.hall.mybatis.mapper.DbMailMapper;
import game.hall.mybatis.mapper.DbMailUserMapper;
import game.hall.mybatis.service.DbUserService;
import game.hall.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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
    private DbMailMapper dbMailMapper;
    @Autowired
    private DbMailAttachmentMapper dbMailAttachmentMapper;
    @Autowired
    private DbMailUserMapper dbMailUserMapper;

    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60;
    private static final String GUEST_DEVICE_USERNAME_PREFIX = "guest_device_";
    private static final int MAX_DEVICE_ID_LENGTH = 40;
    private static final long NEW_USER_GIFT_GOLD = 1000L;
    private static final long NEW_USER_MAIL_EXPIRE_TIME = 30L * 24 * 60 * 60 * 1000;
    private static final int MAIL_TYPE_PERSONAL = 1;
    private static final int MAIL_STATUS_ENABLE = 1;
    private static final int MAIL_STATUS_NO = 0;
    private static final int ITEM_TYPE_GOLD = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
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

    @Override
    public ServerMsg loginByPassword(LoginTokenReq req) {
        if (req == null || StringUtils.isBlank(req.getUsername()) || StringUtils.isBlank(req.getPwd())) {
            return ServerMsg.error(ErrorCode.PARAM_ERROR);
        }

        DbUser dbUser = getUserByUsername(req.getUsername().trim());
        if (dbUser == null || !req.getPwd().equals(dbUser.getPwd())) {
            return ServerMsg.error(ErrorCode.LOGIN_ERROR);
        }

        cacheUser(dbUser);
        return buildLoginResp(dbUser);
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

        sendNewUserGiftMail(dbUser.getId());
        cacheUser(dbUser);
        return buildLoginResp(dbUser);
    }

    private void sendNewUserGiftMail(Long userId) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expireTime = new Date(nowMillis + NEW_USER_MAIL_EXPIRE_TIME);

        DbMail mail = new DbMail();
        mail.setMailType(MAIL_TYPE_PERSONAL);
        mail.setTitle("新手金币礼包");
        mail.setContent("欢迎来到休闲游戏，赠送金币助你开启第一局，记得及时领取。");
        mail.setSender("系统");
        mail.setStartTime(now);
        mail.setExpireTime(expireTime);
        mail.setStatus(MAIL_STATUS_ENABLE);
        mail.setCreateTime(now);
        mail.setUpdateTime(now);
        if (dbMailMapper.insert(mail) <= 0 || mail.getId() == null) {
            throw new IllegalStateException("新用户礼包邮件创建失败");
        }

        DbMailAttachment attachment = new DbMailAttachment();
        attachment.setMailId(mail.getId());
        attachment.setItemType(ITEM_TYPE_GOLD);
        attachment.setItemId(0);
        attachment.setItemCount(NEW_USER_GIFT_GOLD);
        attachment.setCreateTime(now);
        if (dbMailAttachmentMapper.insert(attachment) <= 0) {
            throw new IllegalStateException("新用户礼包附件创建失败");
        }

        DbMailUser mailUser = new DbMailUser();
        mailUser.setUserId(userId);
        mailUser.setMailId(mail.getId());
        mailUser.setReadStatus(MAIL_STATUS_NO);
        mailUser.setReceiveStatus(MAIL_STATUS_NO);
        mailUser.setDeleteStatus(MAIL_STATUS_NO);
        mailUser.setCreateTime(now);
        if (dbMailUserMapper.insert(mailUser) <= 0) {
            throw new IllegalStateException("新用户礼包邮件关联失败");
        }
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

package game.hall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import game.common.constant.PropCodeEnum;
import game.common.service.UserService;
import game.hall.entity.req.SendRewardMailReq;
import game.hall.entity.res.MailAttachmentVO;
import game.hall.entity.res.MailReceiveResultVO;
import game.hall.entity.res.MailVO;
import game.hall.exception.HallException;
import game.hall.mybatis.domain.DbMail;
import game.hall.mybatis.domain.DbMailAttachment;
import game.hall.mybatis.domain.DbMailGlobalReceive;
import game.hall.mybatis.domain.DbMailUser;
import game.hall.mybatis.mapper.DbMailAttachmentMapper;
import game.hall.mybatis.mapper.DbMailGlobalReceiveMapper;
import game.hall.mybatis.mapper.DbMailMapper;
import game.hall.mybatis.mapper.DbMailUserMapper;
import game.hall.service.HallMailService;
import game.hall.service.UserBagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HallMailServiceImpl implements HallMailService {
    private static final int MAIL_TYPE_PERSONAL = 1;
    private static final int MAIL_TYPE_GLOBAL = 2;

    private static final int STATUS_ENABLE = 1;
    private static final int STATUS_NO = 0;
    private static final int STATUS_YES = 1;
    private static final int RECEIVE_NONE = 2;

    private static final int ITEM_TYPE_GOLD = 1;
    private static final int ITEM_TYPE_ROOM_CARD = 2;
    private static final int ITEM_TYPE_DIAMOND = 3;
    private static final int DEFAULT_REWARD_MAIL_EXPIRE_DAYS = 30;
    private static final int MAX_REWARD_MAIL_EXPIRE_DAYS = 30;
    private static final long MAX_REWARD_ATTACHMENT_COUNT = 1000000L;

    @Autowired
    private DbMailMapper dbMailMapper;

    @Autowired
    private DbMailAttachmentMapper dbMailAttachmentMapper;

    @Autowired
    private DbMailUserMapper dbMailUserMapper;

    @Autowired
    private DbMailGlobalReceiveMapper dbMailGlobalReceiveMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserBagService userBagService;

    @Override
    public IPage<MailVO> page(Long userId, Integer pageNo, Integer pageSize) {
        List<MailVO> allMails = listVisibleMails(userId);
        allMails.sort(Comparator.comparing(MailVO::getCreateTime, Comparator.nullsLast(Long::compareTo)).reversed());

        int safePageNo = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : pageSize;
        int fromIndex = Math.min((safePageNo - 1) * safePageSize, allMails.size());
        int toIndex = Math.min(fromIndex + safePageSize, allMails.size());

        Page<MailVO> page = new Page<>(safePageNo, safePageSize, allMails.size());
        page.setRecords(allMails.subList(fromIndex, toIndex));
        return page;
    }

    @Override
    public Integer unreadCount(Long userId) {
        return (int) listVisibleMails(userId)
                .stream()
                .filter(mail -> !Integer.valueOf(STATUS_YES).equals(mail.getReadStatus()))
                .count();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MailVO read(Long userId, Long mailId) {
        DbMail mail = getVisibleMail(mailId);
        Map<Long, List<DbMailAttachment>> attachmentMap = loadAttachmentMap(Collections.singletonList(mailId));

        if (isGlobalMail(mail)) {
            DbMailGlobalReceive status = getOrCreateGlobalStatus(userId, mailId, hasAttachment(attachmentMap, mailId));
            assertNotDeleted(status);
            if (!Integer.valueOf(STATUS_YES).equals(status.getReadStatus())) {
                status.setReadStatus(STATUS_YES);
                status.setReadTime(new Date());
                dbMailGlobalReceiveMapper.updateById(status);
            }
            return convert(mail, status, attachmentMap.get(mailId));
        }

        DbMailUser status = getPersonalStatus(userId, mailId);
        if (!Integer.valueOf(STATUS_YES).equals(status.getReadStatus())) {
            status.setReadStatus(STATUS_YES);
            status.setReadTime(new Date());
            dbMailUserMapper.updateById(status);
        }
        return convert(mail, status, attachmentMap.get(mailId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MailReceiveResultVO receive(Long userId, Long mailId) {
        MailReceiveResultVO result = new MailReceiveResultVO();
        result.setAttachments(receiveOne(userId, mailId, true));
        result.setUser(userService.getUserById(userId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MailReceiveResultVO receiveAll(Long userId) {
        List<MailAttachmentVO> received = new ArrayList<>();
        List<MailVO> mails = listVisibleMails(userId);
        for (MailVO mail : mails) {
            if (!Boolean.TRUE.equals(mail.getHasAttachment())
                    || !Integer.valueOf(STATUS_NO).equals(mail.getReceiveStatus())) {
                continue;
            }

            received.addAll(receiveOne(userId, mail.getMailId(), false));
        }

        MailReceiveResultVO result = new MailReceiveResultVO();
        result.setAttachments(received);
        result.setUser(userService.getUserById(userId));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long mailId) {
        DbMail mail = getVisibleMail(mailId);
        Map<Long, List<DbMailAttachment>> attachmentMap = loadAttachmentMap(Collections.singletonList(mailId));
        boolean hasAttachment = hasAttachment(attachmentMap, mailId);
        Date now = new Date();

        if (isGlobalMail(mail)) {
            DbMailGlobalReceive status = getOrCreateGlobalStatus(userId, mailId, hasAttachment);
            assertNotDeleted(status);
            if (hasAttachment && Integer.valueOf(STATUS_NO).equals(status.getReceiveStatus())) {
                throw new HallException("请先领取附件");
            }

            status.setDeleteStatus(STATUS_YES);
            status.setDeleteTime(now);
            dbMailGlobalReceiveMapper.updateById(status);
            return;
        }

        DbMailUser status = getPersonalStatus(userId, mailId);
        if (hasAttachment && Integer.valueOf(STATUS_NO).equals(status.getReceiveStatus())) {
            throw new HallException("请先领取附件");
        }

        status.setDeleteStatus(STATUS_YES);
        status.setDeleteTime(now);
        dbMailUserMapper.updateById(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendRewardMail(Long operatorUserId, SendRewardMailReq req) {
        validateRewardMailReq(req, operatorUserId);

        Date now = new Date();
        int expireDays = req.getExpireDays() == null || req.getExpireDays() <= 0
                ? DEFAULT_REWARD_MAIL_EXPIRE_DAYS
                : Math.min(req.getExpireDays(), MAX_REWARD_MAIL_EXPIRE_DAYS);

        DbMail mail = new DbMail();
        mail.setMailType(MAIL_TYPE_PERSONAL);
        mail.setTitle(req.getTitle().trim());
        mail.setContent(req.getContent().trim());
        mail.setSender(StringUtils.isBlank(req.getSender()) ? "系统" : req.getSender().trim());
        mail.setStartTime(now);
        mail.setExpireTime(new Date(now.getTime() + expireDays * 86400000L));
        mail.setStatus(STATUS_ENABLE);
        mail.setCreateTime(now);
        mail.setUpdateTime(now);
        if (dbMailMapper.insert(mail) <= 0 || mail.getId() == null) {
            throw new HallException("奖励邮件创建失败");
        }

        for (SendRewardMailReq.Attachment attachmentReq : req.getAttachments()) {
            DbMailAttachment attachment = new DbMailAttachment();
            attachment.setMailId(mail.getId());
            attachment.setItemType(attachmentReq.getItemType());
            attachment.setItemId(attachmentReq.getItemId() == null ? 0 : attachmentReq.getItemId());
            attachment.setItemCount(attachmentReq.getItemCount());
            attachment.setCreateTime(now);
            if (dbMailAttachmentMapper.insert(attachment) <= 0) {
                throw new HallException("奖励邮件附件创建失败");
            }
        }

        DbMailUser mailUser = new DbMailUser();
        mailUser.setUserId(req.getUserId());
        mailUser.setMailId(mail.getId());
        mailUser.setReadStatus(STATUS_NO);
        mailUser.setReceiveStatus(STATUS_NO);
        mailUser.setDeleteStatus(STATUS_NO);
        mailUser.setCreateTime(now);
        if (dbMailUserMapper.insert(mailUser) <= 0) {
            throw new HallException("奖励邮件用户关系创建失败");
        }

        return mail.getId();
    }


    private void validateRewardMailReq(SendRewardMailReq req, Long operatorUserId) {
        if (req == null) {
            throw new HallException("请求参数不能为空");
        }
        if (req.getUserId() == null || req.getUserId() <= 0) {
            throw new HallException("用户ID不能为空");
        }
        if (userService.getUserById(req.getUserId()) == null) {
            throw new HallException("用户不存在");
        }
        if (StringUtils.isBlank(req.getTitle())) {
            throw new HallException("邮件标题不能为空");
        }
        if (StringUtils.isBlank(req.getContent())) {
            throw new HallException("邮件内容不能为空");
        }
        if (req.getAttachments() == null || req.getAttachments().isEmpty()) {
            throw new HallException("奖励附件不能为空");
        }
        if(!operatorUserId.equals(req.getUserId())) {
            throw new HallException("用户校验不匹配");
        }
        for (SendRewardMailReq.Attachment attachment : req.getAttachments()) {
            validateRewardAttachment(attachment);
        }
    }

    private void validateRewardAttachment(SendRewardMailReq.Attachment attachment) {
        if (attachment == null) {
            throw new HallException("奖励附件不能为空");
        }
        if (!Integer.valueOf(ITEM_TYPE_GOLD).equals(attachment.getItemType())
                && !Integer.valueOf(ITEM_TYPE_ROOM_CARD).equals(attachment.getItemType())
                && !Integer.valueOf(ITEM_TYPE_DIAMOND).equals(attachment.getItemType())) {
            throw new HallException("暂不支持该奖励类型");
        }
        if (attachment.getItemCount() == null || attachment.getItemCount() <= 0) {
            throw new HallException("奖励数量必须大于0");
        }
        if (attachment.getItemCount() > MAX_REWARD_ATTACHMENT_COUNT) {
            throw new HallException("单个奖励数量过大");
        }
    }

    private List<MailAttachmentVO> receiveOne(Long userId, Long mailId, boolean failIfEmpty) {
        DbMail mail = getVisibleMail(mailId);
        List<DbMailAttachment> attachments = loadAttachments(mailId);
        boolean hasAttachment = attachments != null && !attachments.isEmpty();

        if (!hasAttachment) {
            markNoAttachment(userId, mail);
            if (failIfEmpty) {
                throw new HallException("邮件没有附件");
            }
            return Collections.emptyList();
        }

        if (isGlobalMail(mail)) {
            receiveGlobalMail(userId, mailId);
        } else {
            receivePersonalMail(userId, mailId);
        }

        attachments.forEach(attachment -> grantAttachment(userId, attachment));
        return attachments.stream().map(this::convertAttachment).collect(Collectors.toList());
    }

    private List<MailVO> listVisibleMails(Long userId) {
        Date now = new Date();
        List<MailVO> result = new ArrayList<>();

        List<DbMailUser> personalStatuses = dbMailUserMapper.selectList(
                Wrappers.<DbMailUser>lambdaQuery()
                        .eq(DbMailUser::getUserId, userId)
                        .eq(DbMailUser::getDeleteStatus, STATUS_NO)
        );

        List<Long> personalMailIds = personalStatuses.stream()
                .map(DbMailUser::getMailId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<DbMail> personalMails = personalMailIds.isEmpty()
                ? Collections.emptyList()
                : dbMailMapper.selectList(activeMailWrapper(now).in(DbMail::getId, personalMailIds));

        List<DbMail> globalMails = dbMailMapper.selectList(
                activeMailWrapper(now).eq(DbMail::getMailType, MAIL_TYPE_GLOBAL)
        );

        Set<Long> allMailIds = new HashSet<>();
        personalMails.forEach(mail -> allMailIds.add(mail.getId()));
        globalMails.forEach(mail -> allMailIds.add(mail.getId()));
        Map<Long, List<DbMailAttachment>> attachmentMap = loadAttachmentMap(new ArrayList<>(allMailIds));

        Map<Long, DbMailUser> personalStatusMap = personalStatuses.stream()
                .collect(Collectors.toMap(DbMailUser::getMailId, status -> status, (left, right) -> left));
        personalMails.forEach(mail -> result.add(
                convert(mail, personalStatusMap.get(mail.getId()), attachmentMap.get(mail.getId()))
        ));

        Map<Long, DbMailGlobalReceive> globalStatusMap = loadGlobalStatusMap(userId, globalMails);
        globalMails.forEach(mail -> {
            DbMailGlobalReceive status = globalStatusMap.get(mail.getId());
            if (status != null && Integer.valueOf(STATUS_YES).equals(status.getDeleteStatus())) {
                return;
            }

            result.add(convert(mail, status, attachmentMap.get(mail.getId())));
        });

        return result;
    }

    private LambdaQueryWrapper<DbMail> activeMailWrapper(Date now) {
        return Wrappers.<DbMail>lambdaQuery()
                .eq(DbMail::getStatus, STATUS_ENABLE)
                .and(wrapper -> wrapper.isNull(DbMail::getStartTime).or().le(DbMail::getStartTime, now))
                .and(wrapper -> wrapper.isNull(DbMail::getExpireTime).or().gt(DbMail::getExpireTime, now));
    }

    private DbMail getVisibleMail(Long mailId) {
        if (mailId == null) {
            throw new HallException("邮件ID不能为空");
        }

        DbMail mail = dbMailMapper.selectById(mailId);
        if (mail == null) {
            throw new HallException("邮件不存在");
        }

        if (!Integer.valueOf(STATUS_ENABLE).equals(mail.getStatus())) {
            throw new HallException("邮件已失效");
        }

        Date now = new Date();
        if (mail.getStartTime() != null && mail.getStartTime().after(now)) {
            throw new HallException("邮件暂未生效");
        }

        if (mail.getExpireTime() != null && !mail.getExpireTime().after(now)) {
            throw new HallException("邮件已过期");
        }

        return mail;
    }

    private DbMailUser getPersonalStatus(Long userId, Long mailId) {
        DbMailUser status = dbMailUserMapper.selectOne(
                Wrappers.<DbMailUser>lambdaQuery()
                        .eq(DbMailUser::getUserId, userId)
                        .eq(DbMailUser::getMailId, mailId)
                        .eq(DbMailUser::getDeleteStatus, STATUS_NO)
        );

        if (status == null) {
            throw new HallException("邮件不存在");
        }

        return status;
    }

    private DbMailGlobalReceive getOrCreateGlobalStatus(Long userId, Long mailId, boolean hasAttachment) {
        DbMailGlobalReceive status = dbMailGlobalReceiveMapper.selectOne(
                Wrappers.<DbMailGlobalReceive>lambdaQuery()
                        .eq(DbMailGlobalReceive::getUserId, userId)
                        .eq(DbMailGlobalReceive::getMailId, mailId)
        );

        if (status != null) {
            return status;
        }

        Date now = new Date();
        status = new DbMailGlobalReceive();
        status.setUserId(userId);
        status.setMailId(mailId);
        status.setReadStatus(STATUS_NO);
        status.setReceiveStatus(hasAttachment ? STATUS_NO : RECEIVE_NONE);
        status.setDeleteStatus(STATUS_NO);
        status.setCreateTime(now);
        dbMailGlobalReceiveMapper.insert(status);
        return status;
    }

    private void receivePersonalMail(Long userId, Long mailId) {
        Date now = new Date();
        int updated = dbMailUserMapper.update(
                null,
                Wrappers.<DbMailUser>lambdaUpdate()
                        .eq(DbMailUser::getUserId, userId)
                        .eq(DbMailUser::getMailId, mailId)
                        .eq(DbMailUser::getDeleteStatus, STATUS_NO)
                        .eq(DbMailUser::getReceiveStatus, STATUS_NO)
                        .set(DbMailUser::getReadStatus, STATUS_YES)
                        .set(DbMailUser::getReadTime, now)
                        .set(DbMailUser::getReceiveStatus, STATUS_YES)
                        .set(DbMailUser::getReceiveTime, now)
        );

        if (updated <= 0) {
            throw new HallException("附件已领取");
        }
    }

    private void receiveGlobalMail(Long userId, Long mailId) {
        DbMailGlobalReceive status = getOrCreateGlobalStatus(userId, mailId, true);
        assertNotDeleted(status);
        Date now = new Date();
        int updated = dbMailGlobalReceiveMapper.update(
                null,
                Wrappers.<DbMailGlobalReceive>lambdaUpdate()
                        .eq(DbMailGlobalReceive::getId, status.getId())
                        .eq(DbMailGlobalReceive::getDeleteStatus, STATUS_NO)
                        .eq(DbMailGlobalReceive::getReceiveStatus, STATUS_NO)
                        .set(DbMailGlobalReceive::getReadStatus, STATUS_YES)
                        .set(DbMailGlobalReceive::getReadTime, now)
                        .set(DbMailGlobalReceive::getReceiveStatus, STATUS_YES)
                        .set(DbMailGlobalReceive::getReceiveTime, now)
        );

        if (updated <= 0) {
            throw new HallException("附件已领取");
        }
    }

    private void markNoAttachment(Long userId, DbMail mail) {
        if (isGlobalMail(mail)) {
            DbMailGlobalReceive status = getOrCreateGlobalStatus(userId, mail.getId(), false);
            assertNotDeleted(status);
            status.setReadStatus(STATUS_YES);
            status.setReadTime(new Date());
            status.setReceiveStatus(RECEIVE_NONE);
            dbMailGlobalReceiveMapper.updateById(status);
            return;
        }

        DbMailUser status = getPersonalStatus(userId, mail.getId());
        status.setReadStatus(STATUS_YES);
        status.setReadTime(new Date());
        status.setReceiveStatus(RECEIVE_NONE);
        dbMailUserMapper.updateById(status);
    }

    private Map<Long, DbMailGlobalReceive> loadGlobalStatusMap(Long userId, List<DbMail> globalMails) {
        List<Long> mailIds = globalMails.stream()
                .map(DbMail::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (mailIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return dbMailGlobalReceiveMapper.selectList(
                        Wrappers.<DbMailGlobalReceive>lambdaQuery()
                                .eq(DbMailGlobalReceive::getUserId, userId)
                                .in(DbMailGlobalReceive::getMailId, mailIds)
                )
                .stream()
                .collect(Collectors.toMap(DbMailGlobalReceive::getMailId, status -> status, (left, right) -> left));
    }

    private Map<Long, List<DbMailAttachment>> loadAttachmentMap(List<Long> mailIds) {
        if (mailIds == null || mailIds.isEmpty()) {
            return new HashMap<>();
        }

        return dbMailAttachmentMapper.selectList(
                        Wrappers.<DbMailAttachment>lambdaQuery().in(DbMailAttachment::getMailId, mailIds)
                )
                .stream()
                .collect(Collectors.groupingBy(DbMailAttachment::getMailId));
    }

    private List<DbMailAttachment> loadAttachments(Long mailId) {
        return dbMailAttachmentMapper.selectList(
                Wrappers.<DbMailAttachment>lambdaQuery().eq(DbMailAttachment::getMailId, mailId)
        );
    }

    private boolean hasAttachment(Map<Long, List<DbMailAttachment>> attachmentMap, Long mailId) {
        List<DbMailAttachment> attachments = attachmentMap.get(mailId);
        return attachments != null && !attachments.isEmpty();
    }

    private boolean isGlobalMail(DbMail mail) {
        return mail != null && Integer.valueOf(MAIL_TYPE_GLOBAL).equals(mail.getMailType());
    }

    private void assertNotDeleted(DbMailGlobalReceive status) {
        if (status != null && Integer.valueOf(STATUS_YES).equals(status.getDeleteStatus())) {
            throw new HallException("邮件已删除");
        }
    }

    private MailVO convert(DbMail mail, DbMailUser status, List<DbMailAttachment> attachments) {
        MailVO vo = convertBase(mail, attachments);
        vo.setReadStatus(status == null || status.getReadStatus() == null ? STATUS_NO : status.getReadStatus());
        vo.setReceiveStatus(resolveReceiveStatus(status == null ? null : status.getReceiveStatus(), vo.getHasAttachment()));
        vo.setDeleteStatus(status == null || status.getDeleteStatus() == null ? STATUS_NO : status.getDeleteStatus());
        return vo;
    }

    private MailVO convert(DbMail mail, DbMailGlobalReceive status, List<DbMailAttachment> attachments) {
        MailVO vo = convertBase(mail, attachments);
        vo.setReadStatus(status == null || status.getReadStatus() == null ? STATUS_NO : status.getReadStatus());
        vo.setReceiveStatus(resolveReceiveStatus(status == null ? null : status.getReceiveStatus(), vo.getHasAttachment()));
        vo.setDeleteStatus(status == null || status.getDeleteStatus() == null ? STATUS_NO : status.getDeleteStatus());
        return vo;
    }

    private MailVO convertBase(DbMail mail, List<DbMailAttachment> attachments) {
        List<MailAttachmentVO> attachmentVOS = attachments == null
                ? Collections.emptyList()
                : attachments.stream().map(this::convertAttachment).collect(Collectors.toList());

        MailVO vo = new MailVO();
        vo.setMailId(mail.getId());
        vo.setMailType(mail.getMailType());
        vo.setTitle(mail.getTitle());
        vo.setContent(mail.getContent());
        vo.setSender(mail.getSender());
        vo.setStartTime(toMillis(mail.getStartTime()));
        vo.setExpireTime(toMillis(mail.getExpireTime()));
        vo.setCreateTime(toMillis(mail.getCreateTime()));
        vo.setHasAttachment(!attachmentVOS.isEmpty());
        vo.setAttachments(attachmentVOS);
        return vo;
    }

    private Long toMillis(Date time) {
        return time == null ? null : time.getTime();
    }

    private Integer resolveReceiveStatus(Integer receiveStatus, Boolean hasAttachment) {
        if (!Boolean.TRUE.equals(hasAttachment)) {
            return RECEIVE_NONE;
        }

        return receiveStatus == null ? STATUS_NO : receiveStatus;
    }

    private MailAttachmentVO convertAttachment(DbMailAttachment attachment) {
        MailAttachmentVO vo = new MailAttachmentVO();
        vo.setId(attachment.getId());
        vo.setMailId(attachment.getMailId());
        vo.setItemType(attachment.getItemType());
        vo.setItemId(attachment.getItemId());
        vo.setItemCount(attachment.getItemCount());
        return vo;
    }

    private void grantAttachment(Long userId, DbMailAttachment attachment) {
        Long count = attachment.getItemCount();
        if (count == null || count <= 0) {
            throw new HallException("邮件附件数量错误");
        }

        if (Integer.valueOf(ITEM_TYPE_GOLD).equals(attachment.getItemType())) {
            userService.changeGold(userId, count);
            return;
        }

        if (Integer.valueOf(ITEM_TYPE_ROOM_CARD).equals(attachment.getItemType())) {
            userBagService.changeProp(userId, PropCodeEnum.ROOM_CARD.getCode(), count);
            return;
        }

        if (Integer.valueOf(ITEM_TYPE_DIAMOND).equals(attachment.getItemType())) {
            userBagService.changeProp(userId, PropCodeEnum.DIAMOND.getCode(), count);
            return;
        }

        throw new HallException("暂不支持该附件类型");
    }
}

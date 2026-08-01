package game.hall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import game.common.constant.PropCodeEnum;
import game.common.entity.User;
import game.common.service.UserService;
import game.hall.entity.res.MailAttachmentVO;
import game.hall.entity.res.MailReceiveResultVO;
import game.hall.entity.res.MailVO;
import game.hall.exception.HallException;
import game.hall.mybatis.domain.Mail;
import game.hall.mybatis.domain.MailAttachment;
import game.hall.mybatis.domain.MailGlobalReceive;
import game.hall.mybatis.domain.MailUser;
import game.hall.mybatis.mapper.MailAttachmentMapper;
import game.hall.mybatis.mapper.MailGlobalReceiveMapper;
import game.hall.mybatis.mapper.MailMapper;
import game.hall.mybatis.mapper.MailUserMapper;
import game.hall.service.HallMailService;
import game.hall.service.UserBagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    @Autowired
    private MailMapper mailMapper;

    @Autowired
    private MailAttachmentMapper mailAttachmentMapper;

    @Autowired
    private MailUserMapper mailUserMapper;

    @Autowired
    private MailGlobalReceiveMapper mailGlobalReceiveMapper;

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
        Mail mail = getVisibleMail(mailId);
        Map<Long, List<MailAttachment>> attachmentMap = loadAttachmentMap(Collections.singletonList(mailId));

        if (isGlobalMail(mail)) {
            MailGlobalReceive status = getOrCreateGlobalStatus(userId, mailId, hasAttachment(attachmentMap, mailId));
            assertNotDeleted(status);
            if (!Integer.valueOf(STATUS_YES).equals(status.getReadStatus())) {
                status.setReadStatus(STATUS_YES);
                status.setReadTime(new Date());
                mailGlobalReceiveMapper.updateById(status);
            }
            return convert(mail, status, attachmentMap.get(mailId));
        }

        MailUser status = getPersonalStatus(userId, mailId);
        if (!Integer.valueOf(STATUS_YES).equals(status.getReadStatus())) {
            status.setReadStatus(STATUS_YES);
            status.setReadTime(new Date());
            mailUserMapper.updateById(status);
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
        Mail mail = getVisibleMail(mailId);
        Map<Long, List<MailAttachment>> attachmentMap = loadAttachmentMap(Collections.singletonList(mailId));
        boolean hasAttachment = hasAttachment(attachmentMap, mailId);
        Date now = new Date();

        if (isGlobalMail(mail)) {
            MailGlobalReceive status = getOrCreateGlobalStatus(userId, mailId, hasAttachment);
            assertNotDeleted(status);
            if (hasAttachment && Integer.valueOf(STATUS_NO).equals(status.getReceiveStatus())) {
                throw new HallException("请先领取附件");
            }

            status.setDeleteStatus(STATUS_YES);
            status.setDeleteTime(now);
            mailGlobalReceiveMapper.updateById(status);
            return;
        }

        MailUser status = getPersonalStatus(userId, mailId);
        if (hasAttachment && Integer.valueOf(STATUS_NO).equals(status.getReceiveStatus())) {
            throw new HallException("请先领取附件");
        }

        status.setDeleteStatus(STATUS_YES);
        status.setDeleteTime(now);
        mailUserMapper.updateById(status);
    }

    private List<MailAttachmentVO> receiveOne(Long userId, Long mailId, boolean failIfEmpty) {
        Mail mail = getVisibleMail(mailId);
        List<MailAttachment> attachments = loadAttachments(mailId);
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

        List<MailUser> personalStatuses = mailUserMapper.selectList(
                Wrappers.<MailUser>lambdaQuery()
                        .eq(MailUser::getUserId, userId)
                        .eq(MailUser::getDeleteStatus, STATUS_NO)
        );

        List<Long> personalMailIds = personalStatuses.stream()
                .map(MailUser::getMailId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<Mail> personalMails = personalMailIds.isEmpty()
                ? Collections.emptyList()
                : mailMapper.selectList(activeMailWrapper(now).in(Mail::getId, personalMailIds));

        List<Mail> globalMails = mailMapper.selectList(
                activeMailWrapper(now).eq(Mail::getMailType, MAIL_TYPE_GLOBAL)
        );

        Set<Long> allMailIds = new HashSet<>();
        personalMails.forEach(mail -> allMailIds.add(mail.getId()));
        globalMails.forEach(mail -> allMailIds.add(mail.getId()));
        Map<Long, List<MailAttachment>> attachmentMap = loadAttachmentMap(new ArrayList<>(allMailIds));

        Map<Long, MailUser> personalStatusMap = personalStatuses.stream()
                .collect(Collectors.toMap(MailUser::getMailId, status -> status, (left, right) -> left));
        personalMails.forEach(mail -> result.add(
                convert(mail, personalStatusMap.get(mail.getId()), attachmentMap.get(mail.getId()))
        ));

        Map<Long, MailGlobalReceive> globalStatusMap = loadGlobalStatusMap(userId, globalMails);
        globalMails.forEach(mail -> {
            MailGlobalReceive status = globalStatusMap.get(mail.getId());
            if (status != null && Integer.valueOf(STATUS_YES).equals(status.getDeleteStatus())) {
                return;
            }

            result.add(convert(mail, status, attachmentMap.get(mail.getId())));
        });

        return result;
    }

    private LambdaQueryWrapper<Mail> activeMailWrapper(Date now) {
        return Wrappers.<Mail>lambdaQuery()
                .eq(Mail::getStatus, STATUS_ENABLE)
                .and(wrapper -> wrapper.isNull(Mail::getStartTime).or().le(Mail::getStartTime, now))
                .and(wrapper -> wrapper.isNull(Mail::getExpireTime).or().gt(Mail::getExpireTime, now));
    }

    private Mail getVisibleMail(Long mailId) {
        if (mailId == null) {
            throw new HallException("邮件ID不能为空");
        }

        Mail mail = mailMapper.selectById(mailId);
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

    private MailUser getPersonalStatus(Long userId, Long mailId) {
        MailUser status = mailUserMapper.selectOne(
                Wrappers.<MailUser>lambdaQuery()
                        .eq(MailUser::getUserId, userId)
                        .eq(MailUser::getMailId, mailId)
                        .eq(MailUser::getDeleteStatus, STATUS_NO)
        );

        if (status == null) {
            throw new HallException("邮件不存在");
        }

        return status;
    }

    private MailGlobalReceive getOrCreateGlobalStatus(Long userId, Long mailId, boolean hasAttachment) {
        MailGlobalReceive status = mailGlobalReceiveMapper.selectOne(
                Wrappers.<MailGlobalReceive>lambdaQuery()
                        .eq(MailGlobalReceive::getUserId, userId)
                        .eq(MailGlobalReceive::getMailId, mailId)
        );

        if (status != null) {
            return status;
        }

        Date now = new Date();
        status = new MailGlobalReceive();
        status.setUserId(userId);
        status.setMailId(mailId);
        status.setReadStatus(STATUS_NO);
        status.setReceiveStatus(hasAttachment ? STATUS_NO : RECEIVE_NONE);
        status.setDeleteStatus(STATUS_NO);
        status.setCreateTime(now);
        mailGlobalReceiveMapper.insert(status);
        return status;
    }

    private void receivePersonalMail(Long userId, Long mailId) {
        Date now = new Date();
        int updated = mailUserMapper.update(
                null,
                Wrappers.<MailUser>lambdaUpdate()
                        .eq(MailUser::getUserId, userId)
                        .eq(MailUser::getMailId, mailId)
                        .eq(MailUser::getDeleteStatus, STATUS_NO)
                        .eq(MailUser::getReceiveStatus, STATUS_NO)
                        .set(MailUser::getReadStatus, STATUS_YES)
                        .set(MailUser::getReadTime, now)
                        .set(MailUser::getReceiveStatus, STATUS_YES)
                        .set(MailUser::getReceiveTime, now)
        );

        if (updated <= 0) {
            throw new HallException("附件已领取");
        }
    }

    private void receiveGlobalMail(Long userId, Long mailId) {
        MailGlobalReceive status = getOrCreateGlobalStatus(userId, mailId, true);
        assertNotDeleted(status);
        Date now = new Date();
        int updated = mailGlobalReceiveMapper.update(
                null,
                Wrappers.<MailGlobalReceive>lambdaUpdate()
                        .eq(MailGlobalReceive::getId, status.getId())
                        .eq(MailGlobalReceive::getDeleteStatus, STATUS_NO)
                        .eq(MailGlobalReceive::getReceiveStatus, STATUS_NO)
                        .set(MailGlobalReceive::getReadStatus, STATUS_YES)
                        .set(MailGlobalReceive::getReadTime, now)
                        .set(MailGlobalReceive::getReceiveStatus, STATUS_YES)
                        .set(MailGlobalReceive::getReceiveTime, now)
        );

        if (updated <= 0) {
            throw new HallException("附件已领取");
        }
    }

    private void markNoAttachment(Long userId, Mail mail) {
        if (isGlobalMail(mail)) {
            MailGlobalReceive status = getOrCreateGlobalStatus(userId, mail.getId(), false);
            assertNotDeleted(status);
            status.setReadStatus(STATUS_YES);
            status.setReadTime(new Date());
            status.setReceiveStatus(RECEIVE_NONE);
            mailGlobalReceiveMapper.updateById(status);
            return;
        }

        MailUser status = getPersonalStatus(userId, mail.getId());
        status.setReadStatus(STATUS_YES);
        status.setReadTime(new Date());
        status.setReceiveStatus(RECEIVE_NONE);
        mailUserMapper.updateById(status);
    }

    private Map<Long, MailGlobalReceive> loadGlobalStatusMap(Long userId, List<Mail> globalMails) {
        List<Long> mailIds = globalMails.stream()
                .map(Mail::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (mailIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return mailGlobalReceiveMapper.selectList(
                        Wrappers.<MailGlobalReceive>lambdaQuery()
                                .eq(MailGlobalReceive::getUserId, userId)
                                .in(MailGlobalReceive::getMailId, mailIds)
                )
                .stream()
                .collect(Collectors.toMap(MailGlobalReceive::getMailId, status -> status, (left, right) -> left));
    }

    private Map<Long, List<MailAttachment>> loadAttachmentMap(List<Long> mailIds) {
        if (mailIds == null || mailIds.isEmpty()) {
            return new HashMap<>();
        }

        return mailAttachmentMapper.selectList(
                        Wrappers.<MailAttachment>lambdaQuery().in(MailAttachment::getMailId, mailIds)
                )
                .stream()
                .collect(Collectors.groupingBy(MailAttachment::getMailId));
    }

    private List<MailAttachment> loadAttachments(Long mailId) {
        return mailAttachmentMapper.selectList(
                Wrappers.<MailAttachment>lambdaQuery().eq(MailAttachment::getMailId, mailId)
        );
    }

    private boolean hasAttachment(Map<Long, List<MailAttachment>> attachmentMap, Long mailId) {
        List<MailAttachment> attachments = attachmentMap.get(mailId);
        return attachments != null && !attachments.isEmpty();
    }

    private boolean isGlobalMail(Mail mail) {
        return mail != null && Integer.valueOf(MAIL_TYPE_GLOBAL).equals(mail.getMailType());
    }

    private void assertNotDeleted(MailGlobalReceive status) {
        if (status != null && Integer.valueOf(STATUS_YES).equals(status.getDeleteStatus())) {
            throw new HallException("邮件已删除");
        }
    }

    private MailVO convert(Mail mail, MailUser status, List<MailAttachment> attachments) {
        MailVO vo = convertBase(mail, attachments);
        vo.setReadStatus(status == null || status.getReadStatus() == null ? STATUS_NO : status.getReadStatus());
        vo.setReceiveStatus(resolveReceiveStatus(status == null ? null : status.getReceiveStatus(), vo.getHasAttachment()));
        vo.setDeleteStatus(status == null || status.getDeleteStatus() == null ? STATUS_NO : status.getDeleteStatus());
        return vo;
    }

    private MailVO convert(Mail mail, MailGlobalReceive status, List<MailAttachment> attachments) {
        MailVO vo = convertBase(mail, attachments);
        vo.setReadStatus(status == null || status.getReadStatus() == null ? STATUS_NO : status.getReadStatus());
        vo.setReceiveStatus(resolveReceiveStatus(status == null ? null : status.getReceiveStatus(), vo.getHasAttachment()));
        vo.setDeleteStatus(status == null || status.getDeleteStatus() == null ? STATUS_NO : status.getDeleteStatus());
        return vo;
    }

    private MailVO convertBase(Mail mail, List<MailAttachment> attachments) {
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

    private MailAttachmentVO convertAttachment(MailAttachment attachment) {
        MailAttachmentVO vo = new MailAttachmentVO();
        vo.setId(attachment.getId());
        vo.setMailId(attachment.getMailId());
        vo.setItemType(attachment.getItemType());
        vo.setItemId(attachment.getItemId());
        vo.setItemCount(attachment.getItemCount());
        return vo;
    }

    private void grantAttachment(Long userId, MailAttachment attachment) {
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

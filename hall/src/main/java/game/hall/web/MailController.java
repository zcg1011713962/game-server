package game.hall.web;

import game.common.constant.ErrorCode;
import game.common.context.UserContext;
import game.common.protocol.ServerMsg;
import game.hall.entity.req.MailIdReq;
import game.hall.entity.req.MailListReq;
import game.hall.entity.req.SendRewardMailReq;
import game.hall.exception.HallException;
import game.hall.service.HallMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MailController {

    @Autowired
    private HallMailService hallMailService;

    @PostMapping("/mail/list")
    public ServerMsg list(@RequestBody(required = false) MailListReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        if (req == null) {
            req = new MailListReq();
        }
        return ServerMsg.ok(hallMailService.page(userId, req.getPageNo(), req.getPageSize()));
    }

    @PostMapping("/mail/unread-count")
    public ServerMsg unreadCount() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        return ServerMsg.ok(hallMailService.unreadCount(userId));
    }

    @PostMapping("/mail/read")
    public ServerMsg read(@RequestBody MailIdReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        try {
            return ServerMsg.ok(hallMailService.read(userId, req == null ? null : req.getMailId()));
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/mail/receive")
    public ServerMsg receive(@RequestBody MailIdReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        try {
            return ServerMsg.ok(hallMailService.receive(userId, req == null ? null : req.getMailId()));
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/mail/receive-all")
    public ServerMsg receiveAll() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        try {
            return ServerMsg.ok(hallMailService.receiveAll(userId));
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/mail/delete")
    public ServerMsg delete(@RequestBody MailIdReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        try {
            hallMailService.delete(userId, req == null ? null : req.getMailId());
            return ServerMsg.ok();
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/mail/send-reward")
    public ServerMsg sendReward(@RequestBody SendRewardMailReq req) {
        Long operatorUserId = UserContext.getUserId();
        if (operatorUserId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        try {
            return ServerMsg.ok(hallMailService.sendRewardMail(operatorUserId, req));
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        }
    }
}

package game.hall.web;

import game.common.constant.ErrorCode;
import game.common.context.UserContext;
import game.common.protocol.ServerMsg;
import game.hall.entity.req.SettleRecordReq;
import game.hall.entity.req.TestSettleRecordReq;
import game.hall.exception.HallException;
import game.hall.service.SettleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SettleRecordController {

    @Autowired
    private SettleService settleService;

    @PostMapping("/settle/record")
    public ServerMsg page(@RequestBody SettleRecordReq query) {
        Long userId = UserContext.getUserId();
        if(userId == null){
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }
        return ServerMsg.ok(
                settleService.page(
                        userId,
                        query.getPageNo(),
                        query.getPageSize(),
                        query.getGameId(),
                        query.getRoomId()
                )
        );
    }

    @PostMapping("/settle/record/test-insert")
    public ServerMsg insertTestRecord(@RequestBody TestSettleRecordReq req) {
        Long operatorUserId = UserContext.getUserId();
        if (operatorUserId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }

        try {
            return ServerMsg.ok(settleService.insertTestRecord(operatorUserId, req));
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        }
    }
}

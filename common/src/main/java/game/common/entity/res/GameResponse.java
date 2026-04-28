package game.common.entity.res;

import game.common.constant.ErrorCode;
import game.common.entity.req.GameRequest;
import game.common.protocol.Cmd;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    private String traceId;
    private String gatewayId;
    private Long userId;
    private Long roomId;
    private Cmd cmd;
    private long seq;
    private int code;
    private String msg;
    private Object data;

    /**
     * 1=单人回包
     * 2=房间广播
     */
    private int pushType;

    public static GameResponse error(GameRequest req, ErrorCode errorCode) {
        GameResponse res = new GameResponse();
        res.setTraceId(req.getTraceId());
        res.setGatewayId(req.getGatewayId());
        res.setUserId(req.getUserId());
        res.setCmd(req.getCmd());
        res.setSeq(req.getSeq());
        res.setCode(errorCode.code());
        res.setMsg(errorCode.msg());
        res.setPushType(1);
        return res;
    }

}

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

    public static GameResponse ok(GameRequest req, Cmd cmd, Object data) {
        GameResponse res = new GameResponse();
        res.setTraceId(req.getTraceId());
        res.setGatewayId(req.getGatewayId());
        res.setUserId(req.getUserId());
        res.setRoomId(req.getRoomId());
        res.setCmd(cmd);
        res.setSeq(req.getSeq());
        res.setCode(0);
        res.setMsg("ok");
        res.setData(data);
        res.setPushType(1);
        return res;
    }

    public static GameResponse error(GameRequest req, ErrorCode error) {
        GameResponse res = new GameResponse();
        res.setTraceId(req.getTraceId());
        res.setGatewayId(req.getGatewayId());
        res.setUserId(req.getUserId());
        res.setRoomId(req.getRoomId());
        res.setCmd(req.getCmd());
        res.setSeq(req.getSeq());
        res.setCode(error.code());
        res.setMsg(error.msg());
        res.setPushType(1);
        return res;
    }

    public static GameResponse error(GameRequest req, int code, String msg) {
        GameResponse res = new GameResponse();
        res.setTraceId(req.getTraceId());
        res.setGatewayId(req.getGatewayId());
        res.setUserId(req.getUserId());
        res.setRoomId(req.getRoomId());
        res.setCmd(req.getCmd());
        res.setSeq(req.getSeq());
        res.setCode(code);
        res.setMsg(msg);
        res.setPushType(1);
        return res;
    }

    public static GameResponse push(Long roomId, Cmd cmd, Object data) {
        GameResponse res = new GameResponse();
        res.setRoomId(roomId);
        res.setCmd(cmd);
        res.setSeq(0);
        res.setCode(0);
        res.setMsg("ok");
        res.setData(data);
        res.setPushType(2);
        return res;
    }

}

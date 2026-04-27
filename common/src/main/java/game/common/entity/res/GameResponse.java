package game.common.entity.res;

import game.common.protocol.Cmd;
import lombok.Data;

@Data
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
}

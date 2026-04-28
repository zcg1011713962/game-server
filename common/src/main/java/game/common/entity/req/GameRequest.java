package game.common.entity.req;

import com.alibaba.fastjson2.JSONObject;
import game.common.protocol.Cmd;
import lombok.Builder;
import lombok.Data;

@Data
public class GameRequest {
    private String traceId;
    private String gatewayId;
    private Cmd cmd;
    private long seq;
    private Long userId;
    private Long roomId;
    private JSONObject data;
}

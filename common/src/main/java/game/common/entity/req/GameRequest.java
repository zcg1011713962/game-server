package game.common.entity.req;

import com.alibaba.fastjson2.JSONObject;
import game.common.protocol.Cmd;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameRequest {
    private String traceId;
    private String gatewayId;
    private Cmd cmd;
    private long seq;
    private Long userId;
    private Long roomId;
    private JSONObject data;
}

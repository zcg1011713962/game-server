package game.common.protocol;

import com.alibaba.fastjson2.JSONObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientMsg {
    private Cmd cmd;
    private long seq;
    private JSONObject data;
}
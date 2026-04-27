package game.common.entity;

import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Packet {
    private Long userId;
    private String gatewayId;
    private JSONObject data;
}

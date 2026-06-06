package game.common.entity.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AssetChangePush {

    /**
     * 变更类型：roomCard / gold / diamond
     */
    private String field;

    /**
     * 变化值：-1 / +100
     */
    private Long change;

    /**
     * 最新值
     */
    private Long value;
}

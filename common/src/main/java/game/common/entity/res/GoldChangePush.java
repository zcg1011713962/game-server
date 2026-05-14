package game.common.entity.res;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoldChangePush {

    private long userId;

    private long gold;

    private long changeGold;

    private String reason;
}
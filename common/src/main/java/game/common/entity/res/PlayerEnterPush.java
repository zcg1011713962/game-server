package game.common.entity.res;

import game.common.entity.PlayerDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerEnterPush {

    private Long roomId;

    private PlayerDTO player;
}

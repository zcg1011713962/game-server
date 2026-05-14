package game.common.entity.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerInfoChangePush {

    private long userId;

    private String nickname;

    private String avatar;

    private long gold;
}

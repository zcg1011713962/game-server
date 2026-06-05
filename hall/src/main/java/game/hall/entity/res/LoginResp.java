package game.hall.entity.res;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResp {
    private Long userId;
    private String nickname;
    private String avatar;
    private Long gold;
    private String token;
}
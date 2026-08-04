package game.hall.entity.req;

import lombok.Data;

@Data
public class LoginTokenReq {
    private String username;

    private String pwd;
}

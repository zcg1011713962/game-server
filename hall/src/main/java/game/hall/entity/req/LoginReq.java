package game.hall.entity.req;

import lombok.Data;

@Data
public class LoginReq {
    private String username;
    private String pwd;
}
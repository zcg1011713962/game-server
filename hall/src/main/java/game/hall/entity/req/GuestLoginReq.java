package game.hall.entity.req;

import lombok.Data;

@Data
public class GuestLoginReq {
    private String token;
    private String deviceId;
}

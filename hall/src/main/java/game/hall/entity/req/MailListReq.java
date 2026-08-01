package game.hall.entity.req;

import lombok.Data;

@Data
public class MailListReq {
    private Integer pageNo = 1;

    private Integer pageSize = 20;
}

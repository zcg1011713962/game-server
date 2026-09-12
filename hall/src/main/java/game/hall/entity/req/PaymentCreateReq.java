package game.hall.entity.req;

import lombok.Data;

@Data
public class PaymentCreateReq {

    private String requestId;
    private String amount;
}
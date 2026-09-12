package game.hall.web;

import game.common.constant.ErrorCode;
import game.common.context.UserContext;
import game.common.protocol.ServerMsg;
import game.hall.entity.req.PaymentCreateReq;
import game.hall.entity.req.PaymentHistoryReq;
import game.hall.entity.req.PaymentOrderReq;
import game.hall.exception.HallException;
import game.hall.mybatis.domain.DbHallPaymentOrder;
import game.hall.service.HallPaymentService;
import game.hall.util.QrCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private HallPaymentService paymentService;

    @PostMapping("/config")
    public ServerMsg config() {
        try {
            return ServerMsg.ok(paymentService.getPaymentConfig());
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Get payment config failed", e);
            return ServerMsg.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    @PostMapping("/create")
    public ServerMsg create(@RequestBody PaymentCreateReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }
        try {
            return ServerMsg.ok(toResponse(paymentService.createOrder(userId, req.getRequestId(), req.getAmount())));
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Create payment order failed, userId={}", userId, e);
            return ServerMsg.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    @PostMapping("/status")
    public ServerMsg status(@RequestBody PaymentOrderReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }
        if (req == null) {
            return ServerMsg.error(ErrorCode.PARAM_ERROR);
        }
        try {
            return ServerMsg.ok(toResponse(paymentService.getOrder(userId, req.getOrderId())));
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Get payment order failed, userId={}, orderId={}", userId,
                    req == null ? null : req.getOrderId(), e);
            return ServerMsg.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    @PostMapping("/history")
    public ServerMsg history(@RequestBody PaymentHistoryReq req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ServerMsg.error(ErrorCode.TOKEN_INVALID);
        }
        try {
            List<Map<String, Object>> result = paymentService.getOrderList(userId, req.getPage(), 20)
                    .stream()
                    .map(this::toResponse)
                    .toList();
            return ServerMsg.ok(result);
        } catch (HallException e) {
            return ServerMsg.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Get payment order history failed, userId={}", userId, e);
            return ServerMsg.error(ErrorCode.SYSTEM_ERROR);
        }
    }

    private Map<String, Object> toResponse(DbHallPaymentOrder order) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", order.getOrderId());
        result.put("amount", order.getAmount() == null ? "" : order.getAmount().toPlainString());
        result.put("payAmount", order.getPayAmount() == null ? "" : order.getPayAmount().toPlainString());
        result.put("coins", order.getCoins());
        result.put("address", order.getAddress());
        if (order.getAddress() != null && !order.getAddress().isBlank()) {
            result.put("qrRows", QrCodeUtil.encodeRows(order.getAddress()));
        }
        result.put("network", "USDT-TRC20");
        result.put("status", order.getStatus());
        result.put("createdAt", order.getCreatedAt());
        result.put("expiresAt", order.getExpiresAt());
        result.put("paidAt", order.getPaidAt());
        result.put("txId", order.getTxId());
        return result;
    }
}

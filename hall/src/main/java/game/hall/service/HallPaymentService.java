package game.hall.service;

import game.hall.mybatis.domain.DbHallPaymentOrder;

import java.util.List;
import java.util.Map;

public interface HallPaymentService {

    Map<String, Object> getPaymentConfig();

    DbHallPaymentOrder createOrder(Long userId, String requestId, String amount);

    DbHallPaymentOrder getOrder(Long userId, String orderId);

    List<DbHallPaymentOrder> getOrderList(Long userId, Integer pageNum, Integer pageSize);

}
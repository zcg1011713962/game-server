package game.hall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import game.hall.exception.HallException;
import game.hall.mybatis.domain.DbConfig;
import game.hall.mybatis.domain.DbHallPaymentOrder;
import game.hall.mybatis.mapper.DbConfigMapper;
import game.hall.mybatis.service.DbHallPaymentOrderService;
import game.hall.service.HallPaymentService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class HallPaymentServiceImpl implements HallPaymentService {

    private static final String PAYMENT_CONFIG_KEY = "payment";
    private static final String PAYMENT_NETWORK = "USDT-TRC20";
    private static final String PENDING_STATUS = "PENDING";
    private static final String EXPIRED_STATUS = "EXPIRED";
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private DbConfigMapper configMapper;
    @Autowired
    private DbHallPaymentOrderService paymentOrderService;
    @Autowired
    private OkxDepositAddressClient okxDepositAddressClient;

    @Override
    public Map<String, Object> getPaymentConfig() {
        PaymentConfig config = loadPaymentConfig();
        return Map.of(
                "enabled", config.enabled(),
                "network", PAYMENT_NETWORK,
                "coinsPerUsdt", config.coinsPerUsdt(),
                "minAmount", config.minAmount().toPlainString(),
                "maxAmount", config.maxAmount().toPlainString()
        );
    }

    @Override
    public DbHallPaymentOrder createOrder(Long userId, String requestId, String amountText) {
        if (userId == null) {
            throw new HallException("User id is required");
        }
        if (requestId == null || !requestId.matches("[A-Za-z0-9_-]{16,64}")) {
            throw new HallException("Invalid payment request id");
        }

        PaymentConfig config = loadPaymentConfig();
        if (!config.enabled()) {
            throw new HallException("Payment service is disabled");
        }
        BigDecimal amount = parseAmount(amountText, config);
        DbHallPaymentOrder requestOrder = paymentOrderService.getOne(new LambdaQueryWrapper<DbHallPaymentOrder>()
                .eq(DbHallPaymentOrder::getUserId, userId)
                .eq(DbHallPaymentOrder::getRequestId, requestId), false);
        if (requestOrder != null) {
            if (requestOrder.getAmount().compareTo(amount) != 0) {
                throw new HallException("Payment request id is already in use");
            }
            return requestOrder;
        }

        long now = System.currentTimeMillis();
        DbHallPaymentOrder pendingOrder = paymentOrderService.getOne(new LambdaQueryWrapper<DbHallPaymentOrder>()
                .eq(DbHallPaymentOrder::getUserId, userId)
                .eq(DbHallPaymentOrder::getStatus, PENDING_STATUS)
                .gt(DbHallPaymentOrder::getExpiresAt, now), false);
        if (pendingOrder != null) {
            return pendingOrder;
        }

        String address = okxDepositAddressClient.getUsdtTrc20Address(
                config.baseUrl(),
                config.apiKey(),
                config.secretKey(),
                config.passphrase()
        );

        for (int retry = 0; retry < 20; retry++) {
            DbHallPaymentOrder order = new DbHallPaymentOrder();
            order.setOrderId(UUID.randomUUID().toString().replace("-", ""));
            order.setUserId(userId);
            order.setRequestId(requestId);
            order.setAmount(amount);
            order.setPayAmount(createPayAmount(amount, address));
            order.setCoins(amount.multiply(BigDecimal.valueOf(config.coinsPerUsdt())).longValueExact());
            order.setAddress(address);
            order.setStatus(PENDING_STATUS);
            order.setCreatedAt(now);
            order.setExpiresAt(now + config.orderMinutes() * 60_000L);
            try {
                paymentOrderService.save(order);
                return order;
            } catch (DuplicateKeyException exception) {
                DbHallPaymentOrder concurrentOrder = paymentOrderService.getOne(new LambdaQueryWrapper<DbHallPaymentOrder>()
                        .eq(DbHallPaymentOrder::getUserId, userId)
                        .eq(DbHallPaymentOrder::getRequestId, requestId), false);
                if (concurrentOrder != null) {
                    return concurrentOrder;
                }
                // A concurrent order used the same exact amount. Generate another quote.
            }
        }
        throw new HallException("Unable to create a unique payment amount");
    }

    @Override
    public DbHallPaymentOrder getOrder(Long userId, String orderId) {
        if (userId == null || orderId == null || !orderId.matches("[a-f0-9]{32}")) {
            throw new HallException("Invalid payment order id");
        }

        DbHallPaymentOrder order = paymentOrderService.getById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new HallException("Payment order not found");
        }
        expireOrderIfNeeded(order);
        return order;
    }

    @Override
    public List<DbHallPaymentOrder> getOrderList(Long userId, Integer pageNum, Integer pageSize) {
        if (userId == null) {
            throw new HallException("User id is required");
        }

        long current = pageNum == null || pageNum < 1 ? 1 : pageNum;
        long size = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        Page<DbHallPaymentOrder> page = paymentOrderService.page(new Page<>(current, size),
                new LambdaQueryWrapper<DbHallPaymentOrder>()
                        .eq(DbHallPaymentOrder::getUserId, userId)
                        .orderByDesc(DbHallPaymentOrder::getCreatedAt));
        page.getRecords().forEach(this::expireOrderIfNeeded);
        return page.getRecords();
    }

    private BigDecimal parseAmount(String amountText, PaymentConfig config) {
        if (amountText == null || !amountText.matches("[0-9]{1,7}(\\.[0-9]{1,2})?")) {
            throw new HallException("Invalid payment amount");
        }

        BigDecimal amount = new BigDecimal(amountText).setScale(2, RoundingMode.UNNECESSARY);
        if (amount.compareTo(config.minAmount()) < 0 || amount.compareTo(config.maxAmount()) > 0) {
            throw new HallException("Payment amount is outside the allowed range");
        }
        return amount;
    }

    private BigDecimal createPayAmount(BigDecimal amount, String address) {
        for (int retry = 0; retry < 20; retry++) {
            int fraction = ThreadLocalRandom.current().nextInt(1, 1_000_000);
            BigDecimal payAmount = amount.add(BigDecimal.valueOf(fraction, 6)).setScale(6, RoundingMode.UNNECESSARY);
            boolean exists = paymentOrderService.exists(new LambdaQueryWrapper<DbHallPaymentOrder>()
                    .eq(DbHallPaymentOrder::getAddress, address)
                    .eq(DbHallPaymentOrder::getPayAmount, payAmount));
            if (!exists) {
                return payAmount;
            }
        }
        throw new HallException("Unable to create a unique payment amount");
    }

    private void expireOrderIfNeeded(DbHallPaymentOrder order) {
        if (!PENDING_STATUS.equals(order.getStatus()) || order.getExpiresAt() > System.currentTimeMillis()) {
            return;
        }
        order.setStatus(EXPIRED_STATUS);
        paymentOrderService.updateById(order);
    }

    private PaymentConfig loadPaymentConfig() {
        DbConfig dbConfig = configMapper.selectOne(new LambdaQueryWrapper<DbConfig>()
                .eq(DbConfig::getConfigKey, PAYMENT_CONFIG_KEY));
        if (dbConfig == null || dbConfig.getValueStr() == null || dbConfig.getValueStr().isBlank()) {
            throw new HallException("Payment configuration is missing");
        }

        try {
            JSONObject paymentConfig = JSON.parseObject(dbConfig.getValueStr()).getJSONObject("payment");
            JSONObject okxConfig = paymentConfig == null ? null : paymentConfig.getJSONObject("okx");
            if (okxConfig == null) {
                throw new HallException("Payment configuration is invalid");
            }
            return new PaymentConfig(
                    okxConfig.getBooleanValue("enabled"),
                    new BigDecimal(okxConfig.getString("minAmount") == null ? "10" : okxConfig.getString("minAmount")),
                    new BigDecimal(okxConfig.getString("maxAmount") == null ? "10000" : okxConfig.getString("maxAmount")),
                    okxConfig.getIntValue("coinsPerUsdt") == 0 ? 100 : okxConfig.getIntValue("coinsPerUsdt"),
                    okxConfig.getIntValue("orderMinutes") == 0 ? 30 : okxConfig.getIntValue("orderMinutes"),
                    okxConfig.getString("baseUrl") == null ? "https://www.okx.com" : okxConfig.getString("baseUrl"),
                    okxConfig.getString("apiKey") == null ? "" : okxConfig.getString("apiKey"),
                    okxConfig.getString("secretKey") == null ? "" : okxConfig.getString("secretKey"),
                    okxConfig.getString("passphrase") == null ? "" : okxConfig.getString("passphrase")
            );
        } catch (HallException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new HallException("Payment configuration is invalid");
        }
    }

    private record PaymentConfig(boolean enabled, BigDecimal minAmount, BigDecimal maxAmount,
                                 int coinsPerUsdt, int orderMinutes, String baseUrl,
                                 String apiKey, String secretKey, String passphrase) {
    }
}

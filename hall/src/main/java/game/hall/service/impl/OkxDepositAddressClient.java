package game.hall.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import game.hall.exception.HallException;
import game.hall.util.HttpClientUtil;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class OkxDepositAddressClient {

    private static final String DEPOSIT_ADDRESS_PATH = "/api/v5/asset/deposit-address?ccy=USDT&chain=USDT-TRC20";
    private static final String PUBLIC_TIME_PATH = "/api/v5/public/time";
    private static final DateTimeFormatterBuilder TIMESTAMP_FORMATTER = new DateTimeFormatterBuilder().appendInstant(3);

    public String getUsdtTrc20Address(String baseUrl, String apiKey, String secretKey, String passphrase) {
        validateConfig(baseUrl, apiKey, secretKey, passphrase);

        String timestamp = TIMESTAMP_FORMATTER.toFormatter().format(Instant.ofEpochMilli(getServerTime(baseUrl)));
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("OK-ACCESS-KEY", apiKey);
        headers.put("OK-ACCESS-SIGN", sign(secretKey, timestamp, "GET", DEPOSIT_ADDRESS_PATH));
        headers.put("OK-ACCESS-TIMESTAMP", timestamp);
        headers.put("OK-ACCESS-PASSPHRASE", passphrase);

        try {
            JSONObject result = JSON.parseObject(HttpClientUtil.get(baseUrl + DEPOSIT_ADDRESS_PATH, headers));
            if (!"0".equals(result.getString("code"))) {
                throw new HallException("Unable to get deposit address");
            }

            JSONArray data = result.getJSONArray("data");
            for (int index = 0; data != null && index < data.size(); index++) {
                JSONObject item = data.getJSONObject(index);
                String tag = item.getString("tag");
                if ("USDT".equals(item.getString("ccy"))
                        && "USDT-TRC20".equals(item.getString("chain"))
                        && (tag == null || tag.isBlank())) {
                    String address = item.getString("addr");
                    if (address.matches("T[1-9A-HJ-NP-Za-km-z]{33}")) {
                        return address;
                    }
                }
            }
        } catch (HallException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new HallException("Unable to get deposit address");
        }
        throw new HallException("USDT-TRC20 deposit address is unavailable");
    }

    private String sign(String secretKey, String timestamp, String method, String requestPath) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] signature = mac.doFinal((timestamp + method + requestPath).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception exception) {
            throw new HallException("Unable to sign OKX request");
        }
    }

    private long getServerTime(String baseUrl) {
        try {
            JSONObject result = JSON.parseObject(HttpClientUtil.get(baseUrl + PUBLIC_TIME_PATH, Map.of()));
            if (!"0".equals(result.getString("code"))) {
                throw new HallException("Unable to get OKX server time");
            }
            JSONArray data = result.getJSONArray("data");
            if (data == null || data.isEmpty() || data.getJSONObject(0).getLongValue("ts") <= 0) {
                throw new HallException("Unable to get OKX server time");
            }
            return data.getJSONObject(0).getLongValue("ts");
        } catch (HallException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new HallException("Unable to get OKX server time");
        }
    }

    private void validateConfig(String baseUrl, String apiKey, String secretKey, String passphrase) {
        if (baseUrl == null || apiKey == null || secretKey == null || passphrase == null
                || baseUrl.isBlank() || apiKey.isBlank() || secretKey.isBlank() || passphrase.isBlank()) {
            throw new HallException("OKX payment configuration is incomplete");
        }
        URI uri = URI.create(baseUrl);
        if (!"https".equals(uri.getScheme())
                || !Set.of("www.okx.com", "openapi.okx.com", "us.okx.com", "eea.okx.com", "tr.okx.com")
                .contains(uri.getHost())
                || uri.getUserInfo() != null
                || uri.getQuery() != null
                || uri.getFragment() != null
                || (uri.getPort() != -1 && uri.getPort() != 443)
                || !uri.getPath().isEmpty()) {
            throw new HallException("Invalid OKX API url");
        }
    }
}

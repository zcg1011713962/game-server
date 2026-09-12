package game.hall.util;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public final class HttpClientUtil {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private HttpClientUtil() {
    }

    public static String get(String url, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(java.net.URI.create(url)).GET();
        headers.forEach(builder::header);
        return send(builder.build());
    }

    public static String postJson(String url, Map<String, String> headers, Object body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)));
        headers.forEach(builder::header);
        return send(builder.build());
    }

    public static String send(HttpRequest request) {
        try {
            log.info("HTTP request method={}, url={}, headers={}", request.method(), request.uri(),
                    JSON.toJSONString(maskHeaders(request.headers().map())));
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("HTTP response method={}, url={}, status={}, body={}", request.method(), request.uri(),
                    response.statusCode(), response.body());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("HTTP request failed, status=" + response.statusCode());
            }
            return response.body();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("HTTP request interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("HTTP request failed", exception);
        }
    }

    private static Map<String, Object> maskHeaders(Map<String, ? extends Object> headers) {
        Map<String, Object> result = new LinkedHashMap<>();
        headers.forEach((key, value) -> result.put(key, isSensitiveHeader(key) ? "***" : value));
        return result;
    }

    private static boolean isSensitiveHeader(String headerName) {
        String name = headerName.toLowerCase();
        return name.contains("authorization")
                || name.contains("secret")
                || name.contains("passphrase")
                || name.contains("access-key")
                || name.contains("access-sign")
                || name.contains("signature");
    }
}

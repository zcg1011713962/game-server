package game.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {
    private static final String SECRET = "p7K9vX3qLm2Zt8QwY5HfR0sN6cA1eD4jUoGkB9PzVxC7M2rT";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    // 过期时间（毫秒）= 7天
    private static final long EXPIRE = 7 * 24 * 60 * 60 * 1000L;

    private JwtUtil() {}
    // 获取用户ID
    public static Long getUserId(String token) {
        Claims claims = parseClaims(token);

        Object userId = claims.get("userId");
        if (userId == null) {
            return null;
        }

        return Long.valueOf(userId.toString());
    }

    // 解析 token
    public static Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 生成 Token
    public static String generateToken(Long userId) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("userId", userId)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + EXPIRE))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

}

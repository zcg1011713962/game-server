package game.gomoku.service;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GomokuBotProfileService {
    public static final String REDIS_KEY = "gomoku:bot:list";
    private final StringRedisTemplate redisTemplate;

    public GomokuBotProfileService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<String, Object> selectBot() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        List<Map> bots = JSON.parseArray(cached, Map.class);
        if (bots == null || bots.isEmpty()) return fallbackBot();
        Map selected = bots.get(ThreadLocalRandom.current().nextInt(bots.size()));
        Map<String, Object> bot = new LinkedHashMap<>();
        selected.forEach((key, value) -> bot.put(String.valueOf(key), value));
        return bot.get("id") instanceof Number ? bot : fallbackBot();
    }

    private Map<String, Object> fallbackBot() {
        Map<String, Object> bot = new LinkedHashMap<>();
        bot.put("id", -10001L);
        bot.put("nickname", "Robot");
        bot.put("avatar", "1");
        bot.put("rankName", "Bronze II");
        bot.put("difficulty", 1);
        return Collections.unmodifiableMap(bot);
    }
}

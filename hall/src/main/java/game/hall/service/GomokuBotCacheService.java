package game.hall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.fastjson2.JSON;
import game.hall.mybatis.domain.DbGomokuBot;
import game.hall.mybatis.service.DbGomokuBotService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GomokuBotCacheService implements CommandLineRunner {
    public static final String REDIS_KEY = "gomoku:bot:list";

    private final DbGomokuBotService botService;
    private final StringRedisTemplate redisTemplate;

    public GomokuBotCacheService(DbGomokuBotService botService, StringRedisTemplate redisTemplate) {
        this.botService = botService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void run(String... args) {
        refresh();
    }

    @Scheduled(fixedDelay = 60_000)
    public void refresh() {
        List<Map<String, Object>> bots = botService.list(new LambdaQueryWrapper<DbGomokuBot>()
                        .eq(DbGomokuBot::getEnabled, 1)
                        .orderByAsc(DbGomokuBot::getSortNo)
                        .orderByAsc(DbGomokuBot::getId))
                .stream()
                .map(this::toCacheValue)
                .toList();
        redisTemplate.opsForValue().set(REDIS_KEY, JSON.toJSONString(bots));
    }

    private Map<String, Object> toCacheValue(DbGomokuBot bot) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", bot.getId());
        value.put("nickname", bot.getNickname());
        value.put("avatar", bot.getAvatar());
        value.put("rankName", bot.getRankName());
        value.put("difficulty", bot.getDifficulty());
        return value;
    }
}

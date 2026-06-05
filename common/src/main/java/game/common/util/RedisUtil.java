package game.common.util;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return convert(value, clazz);
    }

    public void hmset(String key, Map<String, Object> map, long expireSeconds) {
        redisTemplate.opsForHash().putAll(key, map);
        if (expireSeconds > 0) {
            redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS);
        }
    }

    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Object hmget(String key, String field) {
        return redisTemplate
                .opsForHash()
                .get(key, field);
    }

    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public void convertAndSend(String channel, Object msg){
        redisTemplate.convertAndSend(channel, msg);
    }


    private <T> T convert(Object value, Class<T> clazz) {
        if (value == null) {
            return null;
        }

        // 已经是目标类型
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        // 转 JSON 再转 Bean（最稳）
        return JSON.parseObject(JSON.toJSONString(value), clazz);
    }

}

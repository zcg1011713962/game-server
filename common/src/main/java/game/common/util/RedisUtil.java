package game.common.util;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
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

    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long seconds) {
        if (seconds <= 0) {
            return false;
        }
        return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public void del(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    public void hset(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void hset(String key, String field, Object value, long expireSeconds) {
        redisTemplate.opsForHash().put(key, field, value);
        expire(key, expireSeconds);
    }

    public Object hget(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    public <T> T hget(String key, String field, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return convert(value, clazz);
    }

    public void hmset(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    public void hmset(String key, Map<String, Object> map, long expireSeconds) {
        redisTemplate.opsForHash().putAll(key, map);
        expire(key, expireSeconds);
    }

    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public <T> T hmget(String key, Class<T> clazz) {
        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
        if (map.isEmpty()) {
            return null;
        }
        return JSON.parseObject(
                JSON.toJSONString(map),
                clazz
        );
    }

    public Object hmget(String key, String fields) {
        return redisTemplate.opsForHash().get(key, fields);
    }

    public List<Object> hmget(String key, Collection<Object> fields) {
        return redisTemplate.opsForHash().multiGet(key, fields);
    }

    public Long hdel(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    public Boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    public Long hincr(String key, String field, long value) {
        return redisTemplate.opsForHash().increment(key, field, value);
    }

    public Double hincr(String key, String field, double value) {
        return redisTemplate.opsForHash().increment(key, field, value);
    }

    public Long hsize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    public void convertAndSend(String channel, Object msg) {
        redisTemplate.convertAndSend(channel, msg);
    }

    private <T> T convert(Object value, Class<T> clazz) {
        if (value == null) {
            return null;
        }

        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }

        return JSON.parseObject(
                JSON.toJSONString(value),
                clazz
        );
    }
}
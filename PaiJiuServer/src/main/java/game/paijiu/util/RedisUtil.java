package game.paijiu.util;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return convert(value, clazz);
    }

    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ================= Common =================

    public void del(String key) {
        redisTemplate.delete(key);
    }

    public void del(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void expire(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    public Long ttl(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    // ================= Hash =================

    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    public void hSetAll(String key, Map<String, Object> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        return (T) redisTemplate.opsForHash().get(key, field);
    }

    public <T> T hGet(String key, String field, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return convert(value, clazz);
    }

    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public Boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    public void hDel(String key, String... fields) {
        redisTemplate.opsForHash().delete(key, (Object[]) fields);
    }

    public Long hIncr(String key, String field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    // ================= Set =================

    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> sMembers(String key) {
        Set<Object> members = redisTemplate.opsForSet().members(key);
        if (members == null) {
            return Collections.emptySet();
        }

        Set<T> result = new HashSet<>();
        for (Object item : members) {
            result.add((T) item);
        }
        return result;
    }

    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    // ================= List =================

    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    public Long rPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T lPop(String key) {
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T rPop(String key) {
        return (T) redisTemplate.opsForList().rightPop(key);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> lRange(String key, long start, long end) {
        List<Object> list = redisTemplate.opsForList().range(key, start, end);
        if (list == null) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();
        for (Object item : list) {
            result.add((T) item);
        }
        return result;
    }

    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ================= Lock =================

    public Boolean tryLock(String key, String value, long seconds) {
        return redisTemplate.opsForValue()
                .setIfAbsent(key, value, seconds, TimeUnit.SECONDS);
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
    }

    // ================= 核心转换方法 =================

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
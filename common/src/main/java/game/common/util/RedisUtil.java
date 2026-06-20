package game.common.util;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> CHANGE_ASSET_SCRIPT;

    static {
        CHANGE_ASSET_SCRIPT = new DefaultRedisScript<>();
        CHANGE_ASSET_SCRIPT.setResultType(Long.class);
        CHANGE_ASSET_SCRIPT.setScriptText(
                "local current = tonumber(redis.call('HGET', KEYS[1], ARGV[1]) or '0') "
                        + "local delta = tonumber(ARGV[2]) "
                        + "if delta == nil then "
                        + " return -2 "
                        + "end "
                        + "local after = current + delta "
                        + "if after < 0 then "
                        + " return -1 "
                        + "end "
                        + "redis.call('HINCRBY', KEYS[1], ARGV[1], delta) "
                        + "return after "
        );
    }

    // =========================
    // 普通 Value 缓存
    // =========================

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

    public void expire(String key, long seconds) {
        if (seconds > 0) {
            redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        }
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

    // =========================
    // 普通 Hash 缓存：对象用
    // =========================

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

    public Object hmget(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    // =========================
    // 资产 Hash：金币/房卡/钻石专用
    // 必须使用 StringRedisTemplate
    // =========================

    public void assetHSet(String key, String field, long value) {
        stringRedisTemplate.opsForHash().put(
                key,
                field,
                String.valueOf(value)
        );
    }

    public void assetHSet(String key, String field, long value, long expireSeconds) {
        assetHSet(key, field, value);
        assetExpire(key, expireSeconds);
    }

    public void assetHMSet(String key, Map<String, ?> map) {
        if (map == null || map.isEmpty()) {
            return;
        }

        Map<String, String> data = new HashMap<>();

        map.forEach((field, value) -> {
            if (field != null && value != null) {
                data.put(field, String.valueOf(value));
            }
        });

        if (!data.isEmpty()) {
            stringRedisTemplate.opsForHash().putAll(key, data);
        }
    }

    public void assetHMSet(String key, Map<String, ?> map, long expireSeconds) {
        assetHMSet(key, map);
        assetExpire(key, expireSeconds);
    }

    public Long assetHGetLong(String key, String field) {
        Object value = stringRedisTemplate.opsForHash().get(key, field);

        if (value == null) {
            return 0L;
        }

        return Long.parseLong(String.valueOf(value));
    }

    public Map<Object, Object> assetHMGet(String key) {
        return stringRedisTemplate.opsForHash().entries(key);
    }

    public Long assetHDel(String key, Object... fields) {
        return stringRedisTemplate.opsForHash().delete(key, fields);
    }

    public void assetExpire(String key, long seconds) {
        if (seconds > 0) {
            stringRedisTemplate.expire(key, seconds, TimeUnit.SECONDS);
        }
    }

    /**
     * 资产变更：正数增加，负数扣除
     *
     * 返回：
     * >=0 最新值
     * -1 余额不足
     * -2 参数错误
     */
    public Long changeAsset(String key, String field, long delta) {
        if (!StringUtils.hasText(key) || !StringUtils.hasText(field)) {
            return -2L;
        }
        return stringRedisTemplate.execute(
                CHANGE_ASSET_SCRIPT,
                Collections.singletonList(key),
                field,
                String.valueOf(delta)
        );
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


    /**
     * 右侧入队(JSON)
     */
    public Long rightPush(String key, Object value) {

        if (!StringUtils.hasText(key) || value == null) {
            return 0L;
        }

        return redisTemplate.opsForList().rightPush(
                key,
                JSON.toJSONString(value)
        );
    }


    /**
     * 左侧阻塞出队
     */
    public <T> T leftPop(
            String key,
            long timeout,
            TimeUnit unit,
            Class<T> clazz
    ) {

        if (!StringUtils.hasText(key)) {
            return null;
        }

        Object value = redisTemplate
                .opsForList()
                .leftPop(key, timeout, unit);

        if (value == null) {
            return null;
        }

        if (clazz == String.class) {
            return clazz.cast(value.toString());
        }

        return JSON.parseObject(
                value.toString(),
                clazz
        );
    }





}
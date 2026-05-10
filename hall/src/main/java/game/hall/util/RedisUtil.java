package game.hall.util;

import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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

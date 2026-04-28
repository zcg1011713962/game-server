package game.paijiu.config;

import com.alibaba.fastjson2.JSON;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key 用字符串
        StringRedisSerializer keySerializer = new StringRedisSerializer();

        // value 用 JSON（Fastjson2）
        RedisSerializer<Object> valueSerializer = new RedisSerializer<>() {
            @Override
            public byte[] serialize(Object obj) {
                return obj == null ? null : JSON.toJSONBytes(obj);
            }

            @Override
            public Object deserialize(byte[] bytes) {
                return bytes == null ? null : JSON.parse(bytes);
            }
        };

        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);

        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();

        return template;
    }
}
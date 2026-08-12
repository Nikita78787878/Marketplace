package org.example.marketplace.config;

import org.example.marketplace.dto.product.ProductResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class CacheConfiguration {

    @Bean
    public RedisTemplate<String, ProductResponse> redisProductTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, ProductResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        var serializer = new JacksonJsonRedisSerializer<>(objectMapper, ProductResponse.class);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        // Выставляю, т.к. можно не выставлять а поставить через  afterPropertiesSet но поставиться дефолт выстрел в ногу если кто-то дернет
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        return redisTemplate;
    }
}
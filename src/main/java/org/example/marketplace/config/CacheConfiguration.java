package org.example.marketplace.config;

import org.example.marketplace.dto.product.ProductResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

/**
 * Настройка Redis: сериализация и всё, что связано с кешем.
 *
 * Здесь живёт НИЗКОУРОВНЕВЫЙ вариант — RedisTemplate, которым кеш управляется вручную.
 * Высокоуровневый (@Cacheable через Spring Cache Abstraction) настраивается отдельно,
 * через бин RedisCacheManager: у него своя конфигурация сериализации, и этот RedisTemplate
 * на неё никак не влияет.
 */
@Configuration
public class CacheConfiguration {

    /**
     * RedisTemplate для ручного cache-aside (используется в ProductServiceCache).
     *
     * ObjectMapper здесь — из пакета tools.jackson, а не com.fasterxml.jackson: в Spring Boot 4
     * дефолтным стал Jackson 3, и бин в контексте только этого типа. Со старым импортом
     * не находится ни бин, ни подходящий конструктор сериализатора.
     *
     * По той же причине класс называется JacksonJsonRedisSerializer, а не Jackson2Json...:
     * цифра 2 в названиях Spring Data Redis означает «для Jackson 2». Гайды под Boot 3
     * показывают версии с двойкой — они здесь не подойдут.
     */
    @Bean
    public RedisTemplate<String, ProductResponse> redisProductTemplate(
            RedisConnectionFactory redisConnectionFactory,
            ObjectMapper objectMapper) {

        RedisTemplate<String, ProductResponse> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // Типизированный сериализатор (знает ProductResponse) вместо GenericJacksonJson:
        // шаблон работает ровно с одним типом, поэтому служебное поле @class в JSON не нужно.
        // Обратная сторона Generic-варианта — JSON привязан к имени пакета, и переезд класса
        // ломает весь накопленный кеш.
        var serializer = new JacksonJsonRedisSerializer<>(objectMapper, ProductResponse.class);

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        // Сериализаторов у шаблона четыре, и незаданные молча получают JdkSerializationRedisSerializer
        // (ту самую Java-сериализацию, от которой мы уходим). Для opsForValue хватило бы первых двух,
        // но стоит кому-то взять opsForHash — и в Redis полетят бинарные байты вместо JSON.
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(serializer);

        // afterPropertiesSet() руками звать не надо: RedisTemplate реализует InitializingBean,
        // и Spring сам вызывает его для бинов. Понадобилось бы только при new RedisTemplate<>()
        // вне контейнера — например, в юнит-тесте.
        return redisTemplate;
    }
}
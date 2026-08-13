package org.example.marketplace.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Просто создал отдельный конфигурационный файл, чтобы реализовать AOP Cache Radis.
 */
@Configuration
@EnableCaching

public class CacheConfigurationAOP {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        // Generic, а не типизированный JacksonJsonRedisSerializer: CacheManager один на ВСЕ кеши
        // приложения, и заранее не знает, какой тип окажется в каждом. Generic пишет в JSON
        // служебное поле @class с именем класса и при чтении восстанавливает именно его.
        // С типизированным вариантом второй кеш (например, категорий) молча десериализовался бы
        // в ProductResponse с null-полями и падал ClassCastException далеко от места ошибки.
        // Бонус: только у Generic есть NullValueSerializer для маркера NullValue, которым Spring
        // кеширует null-результаты.
        var jsonSerializer = GenericJacksonJsonRedisSerializer.builder().build();

        // TTL ограничивает время жизни рассинхрона с БД: инвалидация может не доехать
        // (Redis лежал) или проиграть гонку читателю. TTL гарантирует, что протухшее
        // значение умрёт максимум через минуту. Экономия памяти здесь вторична.
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // transactionAware() — ключевая строка. Без неё @CacheEvict срабатывает при возврате
        // из метода, то есть ДО коммита (коммит делает внешний транзакционный прокси), и в этот
        // зазор параллельный GET успевает прогреть кеш ещё не закоммиченными данными.
        // С ней Spring оборачивает кеш в TransactionAwareCacheDecorator и откладывает evict
        // до afterCommit — тот же механизм, что вручную собран в ProductCacheEvictor.
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }

}

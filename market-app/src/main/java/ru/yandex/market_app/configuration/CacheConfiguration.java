package ru.yandex.market_app.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;

@Configuration
public class CacheConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, Item> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        return reactiveRedisTemplate(connectionFactory, objectMapper, Item.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, Order> orderReactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        return reactiveRedisTemplate(connectionFactory, objectMapper, Order.class);
    }

    @Bean
    public ReactiveRedisTemplate<String, Long> countRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, Long> context = RedisSerializationContext.<String, Long>newSerializationContext(new StringRedisSerializer())
            .value(new GenericToStringSerializer<>(Long.class))
            .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private <T> ReactiveRedisTemplate<String, T> reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory, ObjectMapper objectMapper, Class<T> type) {
        SerializationPair<String> stringSerializer = SerializationPair.fromSerializer(new StringRedisSerializer());
        SerializationPair<T> jackson2JsonRedisSerializer = SerializationPair.fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper(), type));

        RedisSerializationContext<String, T> context = RedisSerializationContext.<String, T>newSerializationContext()
            .key(stringSerializer)
            .value(jackson2JsonRedisSerializer)
            .hashKey(stringSerializer)
            .hashValue(jackson2JsonRedisSerializer)
            .build();
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}

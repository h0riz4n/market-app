package ru.yandex.market_app.service;

import java.time.Duration;
import java.util.List;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.repository.OrderItemRepository;
import ru.yandex.market_app.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final Duration TTL = Duration.ofMinutes(5);
    private final String CACHE_NAME = "order";

    private final ReactiveRedisTemplate<String, Order> redisTemplate;
    private final TransactionalOperator tx;
    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;

    public Flux<Order> getAll() {
        var key = "%s:list".formatted(CACHE_NAME);
        return redisTemplate.opsForList()
            .range(key, 0, -1)
            .switchIfEmpty(orderRepo.findAll().flatMap(orders -> {
                return redisTemplate.opsForList()
                    .rightPushAll(key, orders)
                    .then(redisTemplate.expire(key, TTL))
                    .thenReturn(orders); 
            }));
    }

    public Mono<Order> getById(Long id) {
        String key = getKey(id);
        return redisTemplate.opsForValue().get(key)
            .switchIfEmpty(
                getByIdFromRepo(id).flatMap(order -> {
                    return redisTemplate.opsForValue()
                        .set(key, order)
                        .thenReturn(order);
                })
            )
            .switchIfEmpty(
                Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Заказ не найден"))
            );
    }

    @Transactional
    public Mono<Order> buy(List<Item> items) {
        if (items.isEmpty())
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Нет товаров в корзине");

        var newOrder = Order.builder()
            .total(items.stream().mapToInt(item -> item.getPrice() * item.getCartCount()).sum())
            .build();

        return tx.transactional(
            orderRepo.save(newOrder).flatMap(order -> {
                return orderItemRepo.saveAll(order, items)
                    .collectList()
                    .flatMap(orderItems -> {
                        return clearCache()
                            .thenReturn(order.toBuilder().items(orderItems).build());
                    });
            })
        );
    }

    private Mono<Void> clearCache() {
        var scan = ScanOptions.scanOptions()
            .match("%s::*".formatted(CACHE_NAME))
            .count(1000)
            .build();
        
        return redisTemplate
            .scan(scan)
            .flatMap(redisTemplate::delete)
            .then();
    }

    private Mono<Order> getByIdFromRepo(Long id) {
        return orderRepo.findById(id)
            .switchIfEmpty(Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Заказ не найден")));
    }

    private final String getKey(Long id) {
        return "%s::%s".formatted(CACHE_NAME, id);
    }
}

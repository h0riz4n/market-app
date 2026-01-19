package ru.yandex.market_app.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.client.PaymentClient;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Cart;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.repository.OrderItemRepository;
import ru.yandex.market_app.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final Duration TTL = Duration.ofMinutes(5);
    private final String CACHE_NAME = "order";
    
    private final UserService userService;

    private final PaymentClient paymentClient;

    private final ReactiveRedisTemplate<String, Order> redisTemplate;

    private final TransactionalOperator transactionalOperator;

    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;

    public Flux<Order> getAll() {
        return userService.getUserIdFromSecurityContext()
            .flatMapMany(userId -> {
                var key = "%s::%s:list".formatted(CACHE_NAME, userId);
                return redisTemplate.opsForList()
                    .range(key, 0, -1)
                    .switchIfEmpty(
                        orderRepo.findAllByUserId(userId).flatMap(orders -> {
                            return redisTemplate.opsForList()
                                .rightPushAll(key, orders)
                                .then(redisTemplate.expire(key, TTL))
                                .thenReturn(orders);
                        })
                    );
            });
    }

    public Mono<Order> getById(Long id) {
        String key = getKey(id);
        return userService.getUserIdFromSecurityContext()
            .flatMap(userId -> {
                return redisTemplate.opsForValue().get(key).flatMap(order -> {
                    if (!order.getUserId().equals(userId)) {
                        return Mono.error(new ApiServiceException(HttpStatus.FORBIDDEN, "Доступ запрещён"));
                    } else {
                        return Mono.just(order);
                    }
                })
                .switchIfEmpty(
                    orderRepo.findById(id).flatMap(order -> {
                        if (!order.getUserId().equals(userId)) {
                            return Mono.error(new ApiServiceException(HttpStatus.FORBIDDEN, "Доступ запрещён"));
                        } else {
                            return redisTemplate.opsForValue()
                                .set(key, order)
                                .thenReturn(order);
                        }
                    })
                )
                .switchIfEmpty(
                    Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Заказ не найден"))
                );
            });
    }

    public Mono<Order> buy(List<Cart> carts) {
        if (carts.isEmpty()) {
            return Mono.error(
                new ApiServiceException(HttpStatus.BAD_REQUEST, "Нет товаров в корзине")
            );
        }

        var totalSum = carts.stream()
            .mapToInt(cart -> cart.getCount().intValue() * cart.getItem().getPrice())
            .sum();

        return userService.getUserIdFromSecurityContext()
            .flatMap(userId -> {
                var newOrder = Order.builder()
                    .total(totalSum)
                    .userId(userId)
                    .creationDateTime(LocalDateTime.now())
                    .build();
                return paymentClient.getBalanceByUserId(userId)
                    .flatMap(balance -> {
                        var amount = new BigDecimal(newOrder.getTotal());
                        if (amount.compareTo(balance.getBalance()) > 0) {
                            return Mono.error(new RuntimeException("Недостаточно средств"));
                        }
                        return paymentClient.pay(balance.getId(), amount);
                    })
                    .flatMap(paymnet -> buy(carts, newOrder));
            });
    }

    private Mono<Order> buy(List<Cart> carts, Order newOrder) {
        return transactionalOperator.transactional(
            orderRepo.save(newOrder)
                .flatMap(order -> {
                    return orderItemRepo.saveAll(order, toItems(carts))
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

    private List<Item> toItems(List<Cart> carts) {
        return carts.stream()
            .map(cart -> {
                var item = cart.getItem();
                item.setCartCount(cart.getCount());
                return item;
            })
            .toList();
    }

    private final String getKey(Long id) {
        return "%s::%s".formatted(CACHE_NAME, id);
    }
}

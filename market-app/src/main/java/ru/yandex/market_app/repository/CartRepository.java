package ru.yandex.market_app.repository;

import java.util.List;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Cart;

public interface CartRepository {

    Mono<Cart> save(Cart cart);

    Mono<Cart> findByUserIdAndItemId(UUID userId, Long itemId);

    Mono<Long> updateCount(Cart cart, Long count);

    Mono<Long> deleteAllByUserId(UUID userId);

    Mono<Long> deleteByUserIdAndItemId(UUID userId, Long itemId);

    Flux<Cart> findAllByUserId(UUID userId);

    Flux<Cart> findAllByUserIdAndItemIdIn(UUID userId, List<Long> itemIds);
}

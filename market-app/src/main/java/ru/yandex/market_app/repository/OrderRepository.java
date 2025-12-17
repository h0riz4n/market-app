package ru.yandex.market_app.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Order;

public interface OrderRepository {

    Mono<Order> findById(Long id);

    Flux<Order> findAll();

    Mono<Order> save(Order order);

    Mono<Void> deleteAll();
}

package ru.yandex.market_app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Criteria;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Item;

public interface ItemRepository {

    Mono<Item> findById(Long id);

    Mono<Item> findByTitle(String title);

    Flux<Item> findAllByCartCountGreaterThan(Integer count);

    Mono<Page<Item>> findAll(Criteria criteria, Pageable pageable);

    Mono<Long> upadteAll();

    Mono<Long> updateCartCount(Item item);

    Mono<Item> save(Item item);

    Mono<Long> deleteAll();
}

package ru.yandex.market_app.service;

import java.time.Duration;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.model.filter.ItemFilterModel;
import ru.yandex.market_app.repository.ItemRepository;
import ru.yandex.market_app.repository.specification.ItemSpecification;

@Service
@CacheConfig(cacheNames = "item")
@RequiredArgsConstructor
public class ItemService {

    private final Duration TTL = Duration.ofMinutes(5);
    private final String CACHE_NAME = "item";

    private final ReactiveRedisTemplate<String, Long> countRedisTemplate;
    private final ReactiveRedisTemplate<String, Item> itemRedisTemplate;

    private final ItemRepository itemRepo;
    private final TransactionalOperator tx;

    public Mono<Item> getById(Long id) {
        String key = getKey(id);
        return itemRedisTemplate.opsForValue().get(key)
            .switchIfEmpty(
                getByIdFromRepo(id).flatMap(item -> {
                    return itemRedisTemplate.opsForValue()
                        .set(key, item, TTL)
                        .thenReturn(item);
                })
            )
            .switchIfEmpty(
                Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Товар не найден"))
            );
    }

    public Mono<Page<Item>> getAll(
        String search,
        ESortType sort,
        Integer pageNumber,
        Integer pageSize
    ) {
        var filter = ItemFilterModel.builder()
            .search(search)
            .build();

        var sorting = switch (sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Direction.ASC, "title");
            case PRICE -> Sort.by(Direction.ASC, "price");
        };

        var pageable = PageRequest.of(pageNumber, pageSize, sorting);
        var specification = new ItemSpecification();

        var normalizedSearch = search == null ? "all" : search;

        var listKey = getKey(
            "%s:list:%s:%s:%s:%s".formatted(
                CACHE_NAME,
                normalizedSearch,
                sort.name(),
                pageNumber,
                pageSize
            )
        );

        var totalKey = getKey(
            "%s:count".formatted(listKey)
        );

        var cachedItemsMono = itemRedisTemplate.opsForList()
            .range(listKey, 0, -1)
            .collectList()
            .filter(list -> !list.isEmpty());

        var cachedTotalMono = countRedisTemplate.opsForValue().get(totalKey);

        return Mono.zip(cachedItemsMono, cachedTotalMono)
            .map(tuple ->
                (Page<Item>) new PageImpl<>(
                    tuple.getT1(),
                    pageable,
                    tuple.getT2()
                )
            )
            .switchIfEmpty(
                itemRepo.findAll(specification.toCriteria(filter), pageable)
                    .flatMap(page -> {
                        var items = page.getContent();
                        var total = page.getTotalElements();

                        return itemRedisTemplate.opsForList()
                            .rightPushAll(listKey, items)
                            .then(countRedisTemplate.opsForValue().set(totalKey, total))
                            .then(itemRedisTemplate.expire(listKey, TTL))
                            .then(itemRedisTemplate.expire(totalKey, TTL))
                            .thenReturn(page);
                    })
            );
    }

    public Mono<Item> updateCart(Long id, EActionType action) {
        return tx.transactional(
            getByIdFromRepo(id).flatMap(item -> {
                return itemRepo.updateCartCount(updateCartCount(item, action))
                    .then(itemRedisTemplate.opsForValue().set(getKey(id), item, TTL))
                    .thenReturn(item);
            })
        );
    }

    public Flux<Item> getAllInCart() {
        return itemRepo.findAllByCartCountGreaterThan(0);
    }

    public Mono<Long> resetCart() {
        return tx.transactional(
            itemRepo.upadteAll()
                .flatMap(updated ->
                    clearItemCache().thenReturn(updated)
                )
        );
    }

    private Mono<Item> getByIdFromRepo(Long id) {
        return itemRepo.findById(id)
            .switchIfEmpty(Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Товар не найден")));
    }

    private Item updateCartCount(Item item, EActionType action) {
        switch (action) {
            case PLUS -> item.setCartCount(item.getCartCount() + 1);
            case MINUS -> item.setCartCount(item.getCartCount() - 1);
            case DELETE -> item.setCartCount(0);
        }

        if (item.getCartCount() < 0) 
            item.setCartCount(0);

        return item;
    }

    private final String getKey(Long id) {
        return "%s::%s".formatted(CACHE_NAME, id);
    }

    private final String getKey(String id) {
        return "%s::%s".formatted(CACHE_NAME, id);
    }

    private Mono<Void> clearItemCache() {
        var scan = ScanOptions.scanOptions()
            .match("%s::*".formatted(CACHE_NAME))
            .count(1000)
            .build();
        
        return itemRedisTemplate
            .scan(scan)
            .flatMap(itemRedisTemplate::delete)
            .then();
    }
}

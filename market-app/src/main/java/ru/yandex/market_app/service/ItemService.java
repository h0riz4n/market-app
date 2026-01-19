package ru.yandex.market_app.service;

import java.time.Duration;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Cart;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.model.filter.ItemFilterModel;
import ru.yandex.market_app.repository.ItemRepository;
import ru.yandex.market_app.repository.specification.ItemSpecification;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final CartService cartService;

    private final Duration TTL = Duration.ofMinutes(5);
    private final String CACHE_NAME = "item";

    private final ReactiveRedisTemplate<String, Long> countRedisTemplate;
    private final ReactiveRedisTemplate<String, Item> redisTemplate;

    private final ItemRepository itemRepo;

    public Mono<Item> getById(Long id) {
        String key = getKey(id);
        return redisTemplate.opsForValue().get(key)
            .switchIfEmpty(
                itemRepo.findById(id).flatMap(item -> setInCache(key, item))
            )
            .flatMap(item -> {
                return cartService.getCartCountByItemId(id)
                    .map(cartCount -> {
                        item.setCartCount(cartCount);
                        return item;
                    });
            })
            .switchIfEmpty(Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Товар не найден")));
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
        var pageable = PageRequest.of(pageNumber, pageSize, toSort(sort));
        var listKey = getKey(
            "%s:list:%s:%s:%s:%s".formatted(
                CACHE_NAME,
                search == null ? "all" : search,
                sort.name(),
                pageNumber,
                pageSize
            )
        );
        var totalKey = getKey("%s:count".formatted(listKey));

        var cachedItemsMono = redisTemplate.opsForList()
            .range(listKey, 0, -1)
            .collectList()
            .filter(list -> !list.isEmpty());

        var cachedTotalMono = countRedisTemplate.opsForValue().get(totalKey);

        return Mono.zip(cachedItemsMono, cachedTotalMono)
            .map(tuple -> (Page<Item>) new PageImpl<Item>(tuple.getT1(), pageable, tuple.getT2()))
            .switchIfEmpty(getAllFromDbAndSetInCache(filter, pageable, listKey, totalKey))
            .flatMap(this::setCartCountOnItems);
    }

    private Mono<Item> setInCache(String key, Item item) {
        return redisTemplate.opsForValue()
            .set(key, item, TTL)
            .thenReturn(item);
    }

    private Mono<Page<Item>> setCartCountOnItems(Page<Item> page) {
        var itemIds = page.getContent()
            .stream()
            .map(Item::getId)
            .toList();

        return cartService.getAllByItemIdIn(itemIds)
            .collectMap(cart -> cart.getId().itemId(), Cart::getCount)
            .map(cartCounts -> {
                page.getContent().forEach(item -> {
                    item.setCartCount(cartCounts.getOrDefault(item.getId(), 0L));
                });
                return page;
            });
    }

    private Mono<Page<Item>> getAllFromDbAndSetInCache(ItemFilterModel filter, Pageable pageable, String listKey, String totalKey) {
        return itemRepo.findAll(ItemSpecification.toCriteria(filter), pageable)
            .flatMap(page -> {
                return redisTemplate.opsForList()
                    .rightPushAll(listKey, page.getContent())
                    .then(countRedisTemplate.opsForValue().set(totalKey, page.getTotalElements()))
                    .then(redisTemplate.expire(listKey, TTL))
                    .then(redisTemplate.expire(totalKey, TTL))
                    .thenReturn(page);
            });
    }

    private Sort toSort(ESortType sort) {
        return switch (sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Direction.ASC, "title");
            case PRICE -> Sort.by(Direction.ASC, "price");
        };
    }

    private final String getKey(Long id) {
        return "%s::%s".formatted(CACHE_NAME, id);
    }

    private final String getKey(String id) {
        return "%s::%s".formatted(CACHE_NAME, id);
    }
}

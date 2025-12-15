package ru.yandex.market_app.service;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepo;

    public Mono<Item> getById(Long id) {
        return itemRepo.findById(id)
            .switchIfEmpty(Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Товар не найден")));
    }

    public Mono<Page<Item>> getAll(String search, ESortType sort, Integer pageNumber, Integer pageSize) {
        var filter = ItemFilterModel.builder()
            .search(search)
            .build();

        var sorting = switch(sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Direction.ASC, "title");
            case PRICE -> Sort.by(Direction.ASC, "price");
        };

        var specification = new ItemSpecification();

        return itemRepo.findAll(specification.toCriteria(filter), PageRequest.of(pageNumber, pageSize, sorting));
    }

    @Transactional
    public Mono<Item> upadteCart(Long id, EActionType action) {
        return getById(id)
            .map(item -> updateCartCount(item, action))
            .map(item -> {
                itemRepo.updateCartCount(item);
                return item;
            });
    }

    public Flux<Item> getAllInCart() {
        return itemRepo.findAllByCartCountGreaterThan(0);
    }

    @Transactional
    public Mono<Long> resetCart() {
        return itemRepo.upadteAll();
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
}

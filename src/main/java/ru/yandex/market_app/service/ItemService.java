package ru.yandex.market_app.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.entity.ItemEntity_;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.model.filter.ItemFilterModel;
import ru.yandex.market_app.repository.ItemRepository;
import ru.yandex.market_app.repository.specification.ItemSpecification;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepo;

    public ItemEntity getById(Long id) {
        return itemRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Товар не найден"));
    }

    public Page<ItemEntity> getAll(String search, ESortType sort, Integer pageNumber, Integer pageSize) {
        var filter = ItemFilterModel.builder()
            .search(search)
            .build();

        var sorting = switch(sort) {
            case NO -> Sort.unsorted();
            case ALPHA -> Sort.by(Direction.ASC, ItemEntity_.TITLE);
            case PRICE -> Sort.by(Direction.ASC, ItemEntity_.PRICE);
        };

        return itemRepo.findAll(new ItemSpecification(filter), PageRequest.of(pageNumber, pageSize, sorting));
    }

    @Transactional
    public ItemEntity upadteCart(Long id, EActionType action) {
        ItemEntity item = getById(id);

        switch (action) {
            case PLUS -> item.setCartCount(item.getCartCount() + 1);
            case MINUS -> item.setCartCount(item.getCartCount() - 1);
            case DELETE -> item.setCartCount(0);
        }

        if (item.getCartCount() < 0) item.setCartCount(0);

        return itemRepo.save(item);
    }

    public List<ItemEntity> getAllInCart() {
        return itemRepo.findAllByCartCountGreaterThan(0);
    }

    @Transactional
    public void resetCart() {
        itemRepo.upadteAll();
    }
}

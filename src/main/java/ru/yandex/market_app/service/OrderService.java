package ru.yandex.market_app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.entity.OrderEntity;
import ru.yandex.market_app.model.entity.OrderItemEntity;
import ru.yandex.market_app.model.entity.id.OrderItemId;
import ru.yandex.market_app.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;

    public List<OrderEntity> getAll() {
        return orderRepo.findAll();
    }

    public OrderEntity getById(Long id) {
        return orderRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Заказ не найден"));
    }

    @Transactional
    public OrderEntity buy(List<ItemEntity> items) {
        if (items.isEmpty())
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Нет товаров в корзине");

        var newOrder = OrderEntity.builder()
            .total(items.stream().mapToInt(item -> item.getPrice() * item.getCartCount()).sum())
            .build();

        var orderItems = items.stream().map(item -> {
            return OrderItemEntity.builder()
                .id(new OrderItemId(newOrder, item))
                .quantity(item.getCartCount())
                .build();
            })
            .toList();

        newOrder.setItems(orderItems);
        return orderRepo.save(newOrder);
    }
}

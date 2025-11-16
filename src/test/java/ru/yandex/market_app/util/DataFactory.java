package ru.yandex.market_app.util;


import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.entity.OrderEntity;
import ru.yandex.market_app.model.entity.OrderItemEntity;
import ru.yandex.market_app.model.entity.id.OrderItemId;
import ru.yandex.market_app.repository.ItemRepository;
import ru.yandex.market_app.repository.OrderRepository;

public class DataFactory {

    @Autowired
    protected ItemRepository itemRepo;

    @Autowired
    protected OrderRepository orderRepo;

    protected ItemEntity mockItem;
    protected OrderEntity mockOrder;

    @BeforeEach
    public void beforeEach() {
        this.mockItem = createItem();
        this.mockOrder = createOrder(mockItem);
    }

    @AfterEach
    public void afterEach() {
        orderRepo.deleteAll();
        itemRepo.deleteAll();
    }

    private ItemEntity createItem() {
        var item = ItemEntity.builder()
            .title("Футбольный мяч")
            .description("Большой футбольный мяч для игры на улице")
            .price(100)
            .image("image/1.jpg")
            .build();
        return itemRepo.save(item);
    }

    private OrderEntity createOrder(ItemEntity item) {
        var order = OrderEntity.builder()
            .total(item.getPrice())
            .build();
    
        var orderItem = OrderItemEntity.builder()
            .id(new OrderItemId(order, item))
            .quantity(item.getCartCount())
            .build();
        order.setItems(List.of(orderItem));
        return orderRepo.save(order);
    }
}

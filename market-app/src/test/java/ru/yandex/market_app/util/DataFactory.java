package ru.yandex.market_app.util;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.repository.CartRepository;
import ru.yandex.market_app.repository.ItemRepository;
import ru.yandex.market_app.repository.OrderItemRepository;
import ru.yandex.market_app.repository.OrderRepository;

public class DataFactory {

    @Autowired
    protected ItemRepository itemRepo;

    @Autowired
    protected CartRepository cartRepo;

    @Autowired
    protected OrderRepository orderRepo;

    @Autowired
    protected OrderItemRepository orderItemRepo;

    protected Item mockItem;
    protected Order mockOrder;

    @BeforeEach
    public void beforeEach() {
        this.mockItem = createItem().block();
        this.mockOrder = createOrder(mockItem).block();
    }

    @AfterEach
    public void afterEach() {
        orderRepo.deleteAll().then(itemRepo.deleteAll()).block();
    }

    protected Mono<Item> createItem() {
        Item item = Item.builder()
            .title("Футбольный мяч")
            .description("Большой футбольный мяч для игры на улице")
            .price(100)
            .image("image/1.jpg")
            .build();
        return itemRepo.save(item);
    }

    protected Mono<Order> createOrder(Item item) {
        Order order = Order.builder()
            .total(item.getPrice())
            .build();

        return orderRepo.save(order)
            .flatMap(savedOrder ->
                orderItemRepo.saveAll(savedOrder, List.of(item))
                    .collectList()
                    .map(orderItems -> {
                        return savedOrder.toBuilder()
                            .items(orderItems)
                            .build();
                    })
            );
    }
}

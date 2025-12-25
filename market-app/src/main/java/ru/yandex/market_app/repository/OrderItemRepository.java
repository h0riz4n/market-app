package ru.yandex.market_app.repository;

import java.util.List;

import reactor.core.publisher.Flux;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.model.domain.OrderItem;

public interface OrderItemRepository {

    Flux<OrderItem> saveAll(Order order, List<Item> items);
}

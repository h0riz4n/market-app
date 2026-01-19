package ru.yandex.market_app.repository.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.model.domain.OrderItem;
import ru.yandex.market_app.model.domain.id.OrderItemId;
import ru.yandex.market_app.repository.OrderItemRepository;

@Repository
public class OrderItemRepositoryImpl implements OrderItemRepository {
    
    private final DatabaseClient databaseClient;
    private final String schema;

    public OrderItemRepositoryImpl(
        @Value("${market-app.default-database-schema}") String schema,
        DatabaseClient databaseClient
    ) {
        this.schema = schema;
        this.databaseClient = databaseClient;
    }

    @Override
    public Flux<OrderItem> saveAll(Order order, List<Item> items) {
        var sql = """
            INSERT INTO %1$s.order_item (order_id, item_id, quantity)
            VALUES (:order_id, :item_id, :quantity)    
            """;

        return Flux.fromIterable(items)
            .concatMap(item -> {
                return databaseClient.sql(sql.formatted(schema))
                    .bind("order_id", order.getId())
                    .bind("item_id", item.getId())
                    .bind("quantity", item.getCartCount())
                    .map((row, metadata) -> {
                        return OrderItem.builder()
                            .id(new OrderItemId(order.getId(), item.getId()))
                            .item(item)
                            .order(order)
                            .quantity(item.getCartCount())
                            .build();
                    })
                    .one();
            });
    }

    
}

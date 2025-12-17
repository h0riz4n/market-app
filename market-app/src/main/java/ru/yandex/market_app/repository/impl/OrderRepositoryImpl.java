package ru.yandex.market_app.repository.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.model.domain.OrderItem;
import ru.yandex.market_app.model.domain.id.OrderItemId;
import ru.yandex.market_app.repository.OrderRepository;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final DatabaseClient databaseClient;
    private final String schema;

    public OrderRepositoryImpl(
        @Value("${market-app.default-database-schema}") String schema,
        DatabaseClient databaseClient
    ) {
        this.schema = schema;
        this.databaseClient = databaseClient;
    }
    
    @Override
    public Flux<Order> findAll() {
        var sql = """
            SELECT 
                o.id AS order_id,
                o.total AS total,
                oi.quantity AS quantity,
                i.id AS item_id,
                i.title AS title,
                i.description AS description,
                i.price AS price,
                i.image AS image,
                i.cart_count AS cart_count
            FROM %1$s.order o
            LEFT JOIN %1$s.order_item oi
            ON oi.order_id = o.id
            LEFT JOIN %1$s.item i
            ON oi.item_id = i.id
            """;

        return databaseClient.sql(sql.formatted(schema))
            .map((row, metadata) -> {
                return Map.of(
                    "orderId", row.get("order_id", Long.class),
                    "total", row.get("total", Integer.class),
                    "quantity", row.get("quantity", Integer.class),
                    "itemId", row.get("item_id", Long.class),
                    "title", row.get("title", String.class),
                    "description", row.get("description", String.class),
                    "price", row.get("price", Integer.class),
                    "image", row.get("image", String.class),
                    "cartCount", row.get("cart_count", Integer.class)
                );
            })
            .all()
            .filter(m -> m.get("orderId") != null)
            .groupBy(m -> (Long) m.get("orderId"))
            .flatMap(group -> group
                .collectList()
                .map(rows -> buildOrderFromRows(group.key(), rows))
            );
    }

    @Override
    public Mono<Order> findById(Long id) {
        var sql = """
            SELECT
                o.id AS order_id,
                o.total AS total,
                oi.quantity AS quantity,
                i.id AS item_id,
                i.title AS title,
                i.description AS description,
                i.price AS price,
                i.image AS image,
                i.cart_count AS cart_count
            FROM %1$s.order o
            LEFT JOIN %1$s.order_item oi
            ON oi.order_id = o.id
            LEFT JOIN %1$s.item i
            ON oi.item_id = i.id
            WHERE o.id = :id
            """;

        return databaseClient.sql(sql.formatted(schema))
            .bind("id", id)
            .map((row, metadata) -> {
                return Map.of(
                    "orderId", row.get("order_id", Long.class),
                    "total", row.get("total", Integer.class),
                    "quantity", row.get("quantity", Integer.class),
                    "itemId", row.get("item_id", Long.class),
                    "title", row.get("title", String.class),
                    "description", row.get("description", String.class),
                    "price", row.get("price", Integer.class),
                    "image", row.get("image", String.class),
                    "cartCount", row.get("cart_count", Integer.class)
                );
            })
            .all()
            .collectList()
            .filter(rows -> !rows.isEmpty())
            .map(rows -> {
                Map<Long, Order> ordersMap = new HashMap<>();
                rows.forEach(row -> {
                    Long orderId = (Long) row.get("orderId");
                    Order order = ordersMap.computeIfAbsent(orderId, currentOrderId -> {
                        return Order.builder()
                            .id(currentOrderId)
                            .total((Integer) row.get("total"))
                            .items(new ArrayList<>())
                            .build();
                    });

                    order.getItems().add(toOrderItem(row, toItem(row)));
                });
                return ordersMap.values()
                    .stream()
                    .findFirst()
                    .orElse(null);
            });
    }

    private Order buildOrderFromRows(Long orderId, List<? extends Map<String, ? extends Object>> rows) {
        Order order = Order.builder()
            .id(orderId)
            .total((Integer) rows.get(0).get("total"))
            .items(new ArrayList<>())
            .build();

        rows.forEach(row -> {
            order.getItems().add(toOrderItem(row, toItem(row)));
        });

        return order;
    }

    private Item toItem(Map<String, ?> row) {
        return Item.builder()
            .id((Long) row.get("itemId"))
            .title((String) row.get("title"))
            .price((Integer) row.get("price"))
            .description((String) row.get("description"))
            .image((String) row.get("image"))
            .build();
    }

    private OrderItem toOrderItem(Map<String, ?> row, Item item) {
        return OrderItem.builder()
            .id(new OrderItemId((Long) row.get("orderId"), (Long) row.get("itemId")))
            .quantity((Integer) row.get("quantity"))
            .item(item)
            .build();
    }

    @Override
    public Mono<Order> save(Order order) {
        var sql = """
            INSERT INTO %1$s."order" (total) 
            VALUES (:total) 
            RETURNING id
            """;

        return databaseClient.sql(sql.formatted(schema))
            .bind("total", order.getTotal())
            .map((row, metadata) -> {
                return order.toBuilder()
                    .id(row.get("id", Long.class))
                    .build();
            })
            .one();
    }

    @Override
    public Mono<Void> deleteAll() {
        var sql = """
            DELETE FROM %1$s.order    
            """;

        return databaseClient.sql(sql.formatted(schema))
            .fetch()
            .rowsUpdated()
            .then();
    }
}

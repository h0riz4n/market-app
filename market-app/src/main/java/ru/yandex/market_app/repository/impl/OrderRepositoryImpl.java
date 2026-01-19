package ru.yandex.market_app.repository.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    public Flux<Order> findAllByUserId(UUID userId) {
        var sql = """
            SELECT 
                o.id AS order_id,
                o.total AS total,
                oi.quantity AS quantity,
                i.id AS item_id,
                i.title AS title,
                i.description AS description,
                i.price AS price,
                i.image AS image
            FROM %1$s.order o
            LEFT JOIN %1$s.order_item oi
            ON oi.order_id = o.id
            LEFT JOIN %1$s.item i
            ON oi.item_id = i.id
            LEFT JOIN %1$s.cart c
            ON c.user_id = o.user_id
            WHERE o.user_id = :userId
            """;

        return databaseClient.sql(sql.formatted(schema))
            .bind("userId", userId)
            .map((row, metadata) -> {
                return Map.of(
                    "orderId", row.get("order_id", Long.class),
                    "total", row.get("total", Integer.class),
                    "quantity", row.get("quantity", Long.class),
                    "itemId", row.get("item_id", Long.class),
                    "title", row.get("title", String.class),
                    "description", row.get("description", String.class),
                    "price", row.get("price", Integer.class),
                    "image", row.get("image", String.class)
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
                o.user_id AS user_id,
                o.creation_date_time AS creation_date_time,
                oi.quantity AS quantity,
                i.id AS item_id,
                i.title AS title,
                i.description AS description,
                i.price AS price,
                i.image AS image
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
                    "userId", row.get("user_id", UUID.class),
                    "creationDateTime", row.get("creation_date_time", LocalDateTime.class),
                    "quantity", row.get("quantity", Long.class),
                    "itemId", row.get("item_id", Long.class),
                    "title", row.get("title", String.class),
                    "description", row.get("description", String.class),
                    "price", row.get("price", Integer.class),
                    "image", row.get("image", String.class)
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
                            .userId((UUID) row.get("userId"))
                            .creationDateTime((LocalDateTime) row.get("creationDateTime"))
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
            .cartCount((Long) row.get("quantity"))
            .build();
    }

    private OrderItem toOrderItem(Map<String, ?> row, Item item) {
        return OrderItem.builder()
            .id(new OrderItemId((Long) row.get("orderId"), (Long) row.get("itemId")))
            .quantity((Long) row.get("quantity"))
            .item(item)
            .build();
    }

    @Override
    public Mono<Order> save(Order order) {
        var sql = """
            INSERT INTO %1$s."order" (user_id, total, creation_date_time) 
            VALUES (:userId, :total, :creationDateTime) 
            RETURNING id
            """;

        return databaseClient.sql(sql.formatted(schema))
            .bind("userId", order.getUserId())
            .bind("total", order.getTotal())
            .bind("creationDateTime", order.getCreationDateTime())
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

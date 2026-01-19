package ru.yandex.market_app.repository.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import io.r2dbc.spi.Row;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Cart;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.id.CartId;
import ru.yandex.market_app.repository.CartRepository;

@Repository
public class CartRepositoryImpl implements CartRepository {
    
    private final String schema;
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate entityTemplate;

    public CartRepositoryImpl(
        @Value("${market-app.default-database-schema}") String schema,
        DatabaseClient databaseClient,
        R2dbcEntityTemplate entityTemplate
    ) {
        this.schema = schema;
        this.databaseClient = databaseClient;
        this.entityTemplate = entityTemplate;
    }

    @Override
    public Mono<Cart> save(Cart cart) {
        var cartId = cart.getId();
        var sql = """
            INSERT INTO %1$s.cart (user_id, item_id, count)
            VALUES (:user_id, :item_id, :count)
            RETURNING user_id, item_id, count
            """;

        return databaseClient.sql(sql.formatted(schema))
            .bind("user_id", cartId.userId())
            .bind("item_id", cartId.itemId())
            .bind("count", cart.getCount())
            .map((row, metadata) -> cart)
            .one();
    }

    @Override
    public Mono<Cart> findByUserIdAndItemId(UUID userId, Long itemId) {
        return entityTemplate.selectOne(
            Query.query(Criteria.where("user_id").is(userId).and("item_id").is(itemId)), 
            Cart.class
        );
    }

    @Override
    public Mono<Long> updateCount(Cart cart, Long count) {
        var cartId = cart.getId();
        return entityTemplate.update(
            Query.query(Criteria.where("user_id").is(cartId.userId()).and("item_id").is(cartId.itemId())), 
            Update.update("count", count),
            Cart.class
        );
    }

    @Override
    public Mono<Long> deleteAllByUserId(UUID userId) {
        return entityTemplate.delete(
            Query.query(Criteria.where("user_id").is(userId)), 
            Cart.class
        );
    }

    @Override
    public Flux<Cart> findAllByUserId(UUID userId) {
        var sql = """
            SELECT
                c.user_id AS user_id,
                c.count AS cart_count,
                i.id AS item_id,
                i.title AS title,
                i.description AS description,
                i.price AS price,
                i.image AS image
            FROM %1$s.cart c
            LEFT JOIN %1$s.item i
            ON c.item_id = i.id
            WHERE c.user_id = :userId
            """;
    
        return databaseClient.sql(sql.formatted(schema))
            .bind("userId", userId)
            .map((row, metadata) -> {
                var cartId = new CartId(row.get("item_id", Long.class), userId);
                return Cart.builder()
                    .id(cartId)
                    .count(row.get("cart_count", Long.class))
                    .item(toItem(row))
                    .build();
            })
            .all();
    }

    @Override
    public Flux<Cart> findAllByUserIdAndItemIdIn(UUID userId, List<Long> itemIds) {
        return entityTemplate.select(
            Query.query(Criteria.where("user_id").is(userId).and("item_id").in(itemIds)), 
            Cart.class
        );
    }

    @Override
    public Mono<Long> deleteByUserIdAndItemId(UUID userId, Long itemId) {
        return entityTemplate.delete(
            Query.query(Criteria.where("user_id").is(userId).and("item_id").is(itemId)),
            Cart.class
        );
    }

    private Item toItem(Row row) {
        return Item.builder()
            .id(row.get("item_id", Long.class))
            .title(row.get("title", String.class))
            .price(row.get("price", Integer.class))
            .description(row.get("description", String.class))
            .image(row.get("image", String.class))
            .build();
    }
}

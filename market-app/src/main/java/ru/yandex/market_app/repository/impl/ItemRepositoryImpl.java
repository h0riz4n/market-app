package ru.yandex.market_app.repository.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.repository.ItemRepository;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {

    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Item> findById(Long id) {
        return entityTemplate.selectOne(
            Query.query(Criteria.where("id").is(id)), 
            Item.class
        );
    }

    @Override
    public Mono<Item> findByTitle(String title) {
        return entityTemplate.selectOne(
            Query.query(Criteria.where("title").is(title)), 
            Item.class
        );
    }

    @Override
    public Flux<Item> findAllByCartCountGreaterThan(Integer count) {
        return entityTemplate.select(
            Query.query(Criteria.where("cart_count").greaterThan(count)), 
            Item.class
        );
    }

    @Override
    public Mono<Long> upadteAll() {
        return entityTemplate.update(
            Query.query(Criteria.where("cart_count").not(0)), 
            Update.update("cart_count", 0),
            Item.class
        );
    }

    @Override
    public Mono<Page<Item>> findAll(Criteria criteria, Pageable pageable) {
        Query pageQuery = Query.query(criteria).with(pageable);
        Query countQuery = Query.query(criteria);

        Mono<List<Item>> contentMono = entityTemplate.select(pageQuery, Item.class).collectList();
        Mono<Long> countMono = entityTemplate.count(countQuery, Item.class);

        return Mono.zip(contentMono, countMono)
            .map(tuple -> new PageImpl<>(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Item> save(Item item) {
        return entityTemplate.insert(item);
    }

    @Override
    public Mono<Long> updateCartCount(Item item) {
        return entityTemplate.update(
            Query.query(Criteria.where("id").is(item.getId())), 
            Update.update("cart_count", 1), 
            Item.class
        );
    }

    @Override
    public Mono<Long> deleteAll() {
        return entityTemplate.delete(Query.empty(), Item.class);
    }
}

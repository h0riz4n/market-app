package ru.yandex.market_app.repository.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.User;
import ru.yandex.market_app.repository.UserRepository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<User> findById(UUID id) {
        return entityTemplate.selectOne(
            Query.query(Criteria.where("id").is(id)), 
            User.class
        );
    }

    @Override
    public Mono<User> save(User user) {
        return entityTemplate.insert(user);
    }

    @Override
    public Mono<Long> updateLastActionDateTime(User user, LocalDateTime lastActionDateTime) {
        return entityTemplate.update(
            Query.query(Criteria.where("id").is(user.getId())), 
            Update.update("last_action_date_time", lastActionDateTime),
            User.class
        );
    }

    @Override
    public Mono<Long> deleteById(UUID id) {
        return entityTemplate.delete(
            Query.query(Criteria.where("id").is(id)), 
            User.class
        );
    }
}

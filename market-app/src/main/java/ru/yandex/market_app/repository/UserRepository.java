package ru.yandex.market_app.repository;

import java.time.LocalDateTime;
import java.util.UUID;

import reactor.core.publisher.Mono;
import ru.yandex.market_app.model.domain.User;

public interface UserRepository {

    Mono<User> findById(UUID id);

    Mono<User> save(User user);

    Mono<Long> updateLastActionDateTime(User user, LocalDateTime lastActionDateTime);

    Mono<Long> deleteById(UUID id);
}

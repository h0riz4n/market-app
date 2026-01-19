package ru.yandex.market_app.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.client.PaymentClient;
import ru.yandex.market_app.model.domain.User;
import ru.yandex.market_app.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PaymentClient paymentClient;

    private final TransactionalOperator transactionalOperator;
    private final UserRepository userRepo;

    public Mono<UUID> getUserIdFromSecurityContext() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> (OAuth2AuthenticationToken) ctx.getAuthentication())
            .map(auth -> UUID.fromString(auth.getPrincipal().getName()));
    }

    public Mono<User> processUser(UUID userId) {
        return transactionalOperator.transactional(
                getById(userId).flatMap(this::updateLastActionDateTime)
            )
            .switchIfEmpty(createUser(userId));
    }

    private Mono<User> getById(UUID userId) {
        return userRepo.findById(userId);
    }

    private Mono<User> updateLastActionDateTime(User user) {
        var lastActionDateTime = LocalDateTime.now();
        user.setLastActionDateTime(lastActionDateTime);
        return userRepo.updateLastActionDateTime(user, lastActionDateTime)
            .thenReturn(user);
    }

    private Mono<User> createUser(UUID userId) {
        return paymentClient.createBalance(userId)
            .flatMap(balance -> {
                var user = User.builder()
                    .id(userId)
                    .creationDateTime(LocalDateTime.now())
                    .lastActionDateTime(LocalDateTime.now())
                    .build();
                return transactionalOperator.transactional(userRepo.save(user));
            });
    }
}

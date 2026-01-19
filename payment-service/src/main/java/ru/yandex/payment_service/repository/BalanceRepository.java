package ru.yandex.payment_service.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import ru.yandex.payment_service.model.domain.Balance;

@Repository
public interface BalanceRepository extends R2dbcRepository<Balance, Long> {

    Mono<Balance> findByUserId(UUID userId);
}

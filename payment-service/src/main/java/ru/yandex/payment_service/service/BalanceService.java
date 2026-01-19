package ru.yandex.payment_service.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.payment_service.exception.ApiServiceException;
import ru.yandex.payment_service.model.domain.Balance;
import ru.yandex.payment_service.repository.BalanceRepository;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final TransactionalOperator transactionalOperator;
    private final BalanceRepository balanceRepo;

    public Mono<Balance> create(UUID userId, BigDecimal amount) {
        var balance = Balance.builder()
            .userId(userId)
            .balance(amount)
            .build();

        return transactionalOperator.transactional(
            balanceRepo.findByUserId(userId)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ApiServiceException(HttpStatus.CONFLICT, "Баланс уже существует"));
                    } else {
                        return balanceRepo.save(balance);
                    }
                })
        );
    }

    public Mono<Balance> getByUserId(UUID userId) {
        return balanceRepo.findByUserId(userId)
            .switchIfEmpty(Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Баланс не найден")));
    }

    Mono<Balance> withdraw(Long id, BigDecimal amount) {
        return getById(id)
            .flatMap(balance -> {
                if (amount.compareTo(balance.getBalance()) > 0) {
                    return Mono.error(new ApiServiceException(HttpStatus.BAD_REQUEST, "Сумма снятие превышает суммы на балансе"));
                }

                balance.setBalance(balance.getBalance().subtract(amount));
                return balanceRepo.save(balance);
            });
    }

    private Mono<Balance> getById(Long id) {
        return balanceRepo.findById(id)
            .switchIfEmpty(Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Баланс не найден")));
    }
 
}

package ru.yandex.payment_service.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.payment_service.model.domain.Balance;
import ru.yandex.payment_service.repository.BalanceRepository;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final Long BALANCE_ID = 1L;
    private final BalanceRepository balanceRepo;

    public Mono<Balance> get() {
        return balanceRepo.findById(BALANCE_ID);
    }

    public Mono<Balance> withdraw(BigDecimal amount) {
        return get()
            .flatMap(balance -> {
                if (amount.compareTo(balance.getBalance()) > 0) {
                    return Mono.error(new RuntimeException("Сумма снятие превышает суммы на балансе"));
                }

                balance.setBalance(balance.getBalance().subtract(amount));
                return balanceRepo.save(balance);
            });

    }
}

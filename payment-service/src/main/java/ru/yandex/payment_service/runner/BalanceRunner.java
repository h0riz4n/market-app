package ru.yandex.payment_service.runner;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.yandex.payment_service.model.domain.Balance;
import ru.yandex.payment_service.repository.BalanceRepository;

@Component
@Profile({ "!test" })
@RequiredArgsConstructor
public class BalanceRunner implements CommandLineRunner {

    private final Long BALANCE_ID = 1L;
    private final BalanceRepository balanceRepo;

    @Override
    public void run(String... args) throws Exception {
        Balance balance = Balance.builder()
            .balance(new BigDecimal(1000L))
            .build();

        balanceRepo.findById(BALANCE_ID)
            .switchIfEmpty(balanceRepo.save(balance))
            .subscribe();
    }

}

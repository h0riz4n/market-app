package ru.yandex.payment_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.payment_service.model.domain.Payment;
import ru.yandex.payment_service.repository.PaymentRepository;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BalanceService balanceService;

    private final PaymentRepository paymentRepo;
    private final TransactionalOperator transactionalOperator;

    public Mono<Payment> pay(Long balanceId, BigDecimal amount) {
        var payment = Payment.builder()
            .balanceId(balanceId)
            .amount(amount)
            .paymentDateTime(LocalDateTime.now())
            .build();
        return transactionalOperator.transactional(
            balanceService.withdraw(balanceId, amount)
                .then(paymentRepo.save(payment))
        );
    }
}

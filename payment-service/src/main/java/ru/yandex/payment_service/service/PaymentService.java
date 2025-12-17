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

    private final PaymentRepository paymentRepo;

    public Mono<Payment> pay(BigDecimal amount) {
        var payment = Payment.builder()
            .amount(amount)
            .paymentDateTime(LocalDateTime.now())
            .build();
        return paymentRepo.save(payment);
    }
}

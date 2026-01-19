package ru.yandex.market_app.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.client.api.BalanceApi;
import ru.yandex.client.api.PaymentApi;
import ru.yandex.client.model.BalanceRequest;
import ru.yandex.client.model.BalanceResponse;
import ru.yandex.client.model.PaymentRequest;
import ru.yandex.client.model.PaymentResponse;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final PaymentApi paymentApi;
    private final BalanceApi balanceApi;

    public Mono<BalanceResponse> createBalance(UUID userId) {
        var balanceRequest = new BalanceRequest()
            .balance(new BigDecimal(10000))
            .userId(userId);
        return balanceApi.createBalance(balanceRequest);
    }

    public Mono<BalanceResponse> getBalanceByUserId(UUID userId) {
        return balanceApi.getBalance(userId);
    }

    public Mono<PaymentResponse> pay(Long balanceId, BigDecimal amount) {
        var paymentRequest = new PaymentRequest()
            .balanceId(balanceId)
            .amount(amount);
        return paymentApi.makePayment(paymentRequest);
    }
}

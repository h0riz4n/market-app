package ru.yandex.payment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.api.PaymentApi;
import ru.yandex.model.PaymentRequest;
import ru.yandex.model.PaymentResponse;
import ru.yandex.payment_service.mapper.PaymentMapper;
import ru.yandex.payment_service.service.PaymentService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentMapper paymentMapper;
    private final PaymentService paymentService;
    
    @Override
    public Mono<ResponseEntity<PaymentResponse>> makePayment(
        @Parameter(name = "PaymentRequest", description = "", required = true) @Valid @RequestBody Mono<PaymentRequest> paymentRequest,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return paymentRequest
            .flatMap(req -> paymentService.pay(req.getBalanceId(), req.getAmount()))
            .map(paymentMapper::toDto)
            .map(ResponseEntity::ok);
    }
}

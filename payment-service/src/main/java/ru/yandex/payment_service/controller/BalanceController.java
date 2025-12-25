package ru.yandex.payment_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.api.BalanceApi;
import ru.yandex.model.BalanceResponse;
import ru.yandex.payment_service.mapper.BalanceMapper;
import ru.yandex.payment_service.service.BalanceService;

@RestController
@RequiredArgsConstructor
public class BalanceController implements BalanceApi {

    private final BalanceService balanceService;
    private final BalanceMapper balanceMapper;

    public Mono<ResponseEntity<BalanceResponse>> getBalance(
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return balanceService.get()
            .map(balance -> balanceMapper.toDto(balance))
            .map(ResponseEntity::ok);
    }
}

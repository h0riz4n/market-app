package ru.yandex.payment_service.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.api.BalanceApi;
import ru.yandex.model.BalanceRequest;
import ru.yandex.model.BalanceResponse;
import ru.yandex.payment_service.mapper.BalanceMapper;
import ru.yandex.payment_service.service.BalanceService;

@RestController
@RequiredArgsConstructor
public class BalanceController implements BalanceApi {

    private final BalanceService balanceService;
    private final BalanceMapper balanceMapper;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(
        @NotNull @Parameter(name = "id", description = "Идентификатор баланса", required = true, in = ParameterIn.PATH) @PathVariable("userId") UUID userId,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return balanceService.getByUserId(userId)
            .map(balanceMapper::toDto)
            .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> createBalance(
        @Parameter(name = "BalanceRequest", description = "", required = true) @Valid @RequestBody Mono<BalanceRequest> balanceRequest,
        @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return balanceRequest
            .flatMap(request -> {
                return balanceService.create(request.getUserId(), request.getBalance())
                    .map(balanceMapper::toDto)
                    .map(ResponseEntity::ok);
            });
    }
}

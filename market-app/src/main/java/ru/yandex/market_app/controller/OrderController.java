package ru.yandex.market_app.controller;
import java.net.URI;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.mapper.OrderMapper;
import ru.yandex.market_app.model.domain.Cart;
import ru.yandex.market_app.service.CartService;
import ru.yandex.market_app.service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.constraints.Positive;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final CartService cartService;
    private final OrderService orderService;

    private final OrderMapper orderMapper;

    @GetMapping("/orders")
    public Mono<Rendering> getAll() {
        return orderService.getAll()
            .collectList()
            .map(orders -> {
                return Rendering.view("orders")
                    .modelAttribute("orders", orderMapper.toDtos(orders))
                    .build();
            });
    }

    @GetMapping("/orders/{id}")
    public Mono<Rendering> getOrderById(
        @PathVariable("id") @Positive Long id,
        @RequestParam(required = false, defaultValue = "false") Boolean newOrder
    ) {
        return orderService.getById(id)
            .map(order -> {
                return Rendering.view("order")
                    .modelAttribute("order", orderMapper.toDto(order))
                    .modelAttribute("newOrder", newOrder)
                    .build();
            });
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return cartService.getCart()
            .collectList()
            .flatMap(this::redirect);
    }

    private Mono<String> redirect(List<Cart> carts) {
        return orderService.buy(carts)
            .flatMap(order -> {
                return cartService.resetCart().then(Mono.fromSupplier(() -> {
                    URI redirectUri = UriComponentsBuilder.fromPath("/orders/{id}")
                        .queryParam("newOrder", true)
                        .build(order.getId()); 
                    return "redirect:%s".formatted(redirectUri.toString());
                }));
            });
    }
}

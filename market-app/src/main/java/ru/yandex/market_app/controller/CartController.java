package ru.yandex.market_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.mapper.ItemMapper;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.service.ItemService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;

import jakarta.validation.constraints.Positive;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/cart/items")
public class CartController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping
    public Mono<Rendering> getCart() {
        return itemService.getAllInCart()
            .collectList()
            .map(items -> {
                return Rendering.view("cart")
                    .modelAttribute("items", itemMapper.toDtos(items))
                    .modelAttribute("total", items.stream().mapToInt(item -> item.getPrice() * item.getCartCount()).sum())
                    .build();
            });
    }

    @PostMapping
    public Mono<Rendering> updateCart(
        @RequestParam @Positive Long id,
        @RequestParam EActionType action
    ) {
        return itemService.updateCart(id, action)
            .flatMap(item -> {
                return itemService.getAllInCart()
                    .collectList()
                    .map(items -> {
                        return Rendering.view("cart")
                            .modelAttribute("items", itemMapper.toDtos(items))
                            .modelAttribute("total", items.stream().mapToInt(it -> it.getPrice() * it.getCartCount()).sum())
                            .build();
                    });
            });
    }
}

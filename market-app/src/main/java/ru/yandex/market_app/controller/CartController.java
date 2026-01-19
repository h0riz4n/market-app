package ru.yandex.market_app.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.mapper.ItemMapper;
import ru.yandex.market_app.model.domain.Cart;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.service.ItemService;
import ru.yandex.market_app.service.CartService;

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

    private final CartService cartService;

    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping
    public Mono<Rendering> getCart() {
        return cartService.getCart()
            .collectList()
            .map(this::toItems)
            .map(this::toRendering);
    }

    @PostMapping
    public Mono<Rendering> updateCart(@RequestParam @Positive Long id, @RequestParam EActionType action) {
        return itemService.getById(id)
            .flatMap(item -> {
                return cartService.updateCart(id, action)
                    .flatMap(updatedCart -> {
                        return cartService.getCart()
                            .collectList()
                            .map(this::toItems)
                            .map(this::toRendering);
                    });
            });
    }

    private Rendering toRendering(List<Item> items) {
        return Rendering.view("cart")
            .modelAttribute("items", itemMapper.toDtos(items))
            .modelAttribute("total", items.stream().mapToInt(item -> item.getPrice() * item.getCartCount().intValue()).sum())
            .build();
    }

    private List<Item> toItems(List<Cart> carts) {
        return carts.stream()
            .map(cart -> {
                var item = cart.getItem();
                item.setCartCount(cart.getCount());
                return item;
            })
            .toList();
    }
}

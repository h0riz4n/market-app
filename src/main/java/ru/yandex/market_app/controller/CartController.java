package ru.yandex.market_app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.mapper.ItemMapper;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.service.ItemService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

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
    public ModelAndView getCart() {
        var items = itemService.getAllInCart();
        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("items", itemMapper.toDtos(items));
        modelAndView.addObject("total", items.stream().mapToInt(item -> item.getPrice() * item.getCartCount()).sum());
        return modelAndView;
    }

    @PostMapping
    public ModelAndView updateCart(
        @RequestParam @Positive Long id,
        @RequestParam EActionType action
    ) {
        itemService.upadteCart(id, action);
        var items = itemService.getAllInCart();
        ModelAndView modelAndView = new ModelAndView("cart");
        modelAndView.addObject("items", itemMapper.toDtos(items));
        modelAndView.addObject("total", items.stream().mapToInt(item -> item.getPrice() * item.getCartCount()).sum());
        return modelAndView;
    }
    
}

package ru.yandex.market_app.controller;

import java.net.URI;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.mapper.OrderMapper;
import ru.yandex.market_app.service.ItemService;
import ru.yandex.market_app.service.OrderService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.constraints.Positive;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
@Validated
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    private final ItemService itemService;

    @GetMapping("/orders")
    public ModelAndView getAll() {
        var orders = orderService.getAll();
        ModelAndView modelAndView = new ModelAndView("orders");
        modelAndView.addObject("orders", orderMapper.toDtos(orders));
        return modelAndView;
    }

    @GetMapping("/orders/{id}")
    public ModelAndView getOrderById(
        @PathVariable("id") @Positive Long id,
        @RequestParam(required = false, defaultValue = "false") Boolean newOrder
    ) {
        var order = orderService.getById(id);
        ModelAndView modelAndView = new ModelAndView("order");
        modelAndView.addObject("order", orderMapper.toDto(order));
        modelAndView.addObject("newOrder", newOrder);
        return modelAndView;
    }

    @PostMapping("/buy")
    public String buy() {
        var items = itemService.getAllInCart();
        var order = orderService.buy(items);
        itemService.resetCart();
        URI redirectUri = UriComponentsBuilder.fromPath("/orders/{id}")
            .queryParam("newOrder", true)
            .build(order.getId());
        return "redirect:%s".formatted(redirectUri.toString());
    }
}

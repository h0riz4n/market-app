package ru.yandex.market_app.controller;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.mapper.ItemMapper;
import ru.yandex.market_app.model.dto.ItemDto;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.service.ItemService;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@Validated
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    @GetMapping(path = { "/", "items" })
    public ModelAndView getItems(
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "NO") ESortType sort,
        @RequestParam(required = false, defaultValue = "1") @Positive Integer pageNumber,
        @RequestParam(required = false, defaultValue = "5") @Positive Integer pageSize
    ) {
        var items = itemService.getAll(search, sort, pageNumber > 0 ? pageNumber - 1 : 0, pageSize).map(itemMapper::toDto);
        ModelAndView modelAndView = new ModelAndView("items");
        modelAndView.addObject("items", chunk(items.getContent(), 3));
        modelAndView.addObject("search", search);
        modelAndView.addObject("sort", sort.name());
        modelAndView.addObject("paging", new Paging(pageSize, pageNumber, items.hasPrevious(), items.hasNext()));
        return modelAndView;
    }

    @GetMapping("/items/{id}")
    public ModelAndView getById(@PathVariable("id") @Positive Long id) {
        ItemEntity item = itemService.getById(id);
        ModelAndView modelAndView = new ModelAndView("item");
        modelAndView.addObject("item", itemMapper.toDto(item));
        return modelAndView;
    }
    
    @PostMapping("/items/{id}")
    public ModelAndView updateCart(
        @PathVariable("id") @Positive Long id,
        @RequestParam EActionType action
    ) {
        var item = itemService.upadteCart(id, action);
        ModelAndView modelAndView = new ModelAndView("item");
        modelAndView.addObject("item", itemMapper.toDto(item));
        return modelAndView;
    }

    @PostMapping("/items")
    public String updateCart(
        @RequestParam @Positive Long id,
        @RequestParam EActionType action,
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "NO") ESortType sort,
        @RequestParam(required = false, defaultValue = "1") @Positive Integer pageNumber,
        @RequestParam(required = false, defaultValue = "5") @Positive Integer pageSize
    ) {
        itemService.upadteCart(id, action);
        URI redirectUri = UriComponentsBuilder.fromPath("/items")
            .queryParam("search", search)
            .queryParam("sort", sort)
            .queryParam("pageNumber", pageNumber)
            .queryParam("pageSize", pageSize)
            .build()
            .toUri();
        return "redirect:%s".formatted(redirectUri.toString());
    }

    private List<List<ItemDto>> chunk(List<ItemDto> list, int size) {
        AtomicInteger counter = new AtomicInteger(0);
        return list.stream()
            .collect(Collectors.groupingBy(i -> counter.getAndIncrement() / size))
            .values()
            .stream()
            .toList();
    }

    private record Paging(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
    ) { }
}

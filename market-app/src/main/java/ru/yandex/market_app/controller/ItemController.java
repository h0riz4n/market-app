package ru.yandex.market_app.controller;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.mapper.ItemMapper;
import ru.yandex.market_app.model.dto.ItemDto;
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
    public Mono<Rendering> getItems(
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "NO") ESortType sort,
        @RequestParam(required = false, defaultValue = "1") @Positive Integer pageNumber,
        @RequestParam(required = false, defaultValue = "5") @Positive Integer pageSize
    ) {
        return itemService.getAll(search, sort, pageNumber > 0 ? pageNumber - 1 : 0, pageSize)
            .map(page -> page.map(itemMapper::toDto))
            .map(page -> {
                return Rendering.view("items")
                    .modelAttribute("items", chunk(page.getContent(), 3))
                    .modelAttribute("search", search)
                    .modelAttribute("sort", sort.name())
                    .modelAttribute("paging", new Paging(pageSize, pageNumber, page.hasPrevious(), page.hasNext()))
                    .build();
            });
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getById(@PathVariable("id") @Positive Long id) {
        return itemService.getById(id)
            .map(item -> {
                return Rendering.view("item")
                    .modelAttribute("item", itemMapper.toDto(item))
                    .build();
            });
    }
    
    @PostMapping("/items/{id}")
    public Mono<Rendering> updateCart(
        @PathVariable("id") @Positive Long id,
        @RequestParam EActionType action
    ) {
        return itemService.updateCart(id, action)
            .map(item -> {
                return Rendering.view("item")
                    .modelAttribute("item", itemMapper.toDto(item))
                    .build();
            });
    }

    @PostMapping("/items")
    public Mono<String> updateCart(
        @RequestParam @Positive Long id,
        @RequestParam EActionType action,
        @RequestParam(required = false) String search,
        @RequestParam(required = false, defaultValue = "NO") ESortType sort,
        @RequestParam(required = false, defaultValue = "1") @Positive Integer pageNumber,
        @RequestParam(required = false, defaultValue = "5") @Positive Integer pageSize
    ) {
        return itemService.updateCart(id, action)
            .map(item -> {
                URI redirectUri = UriComponentsBuilder.fromPath("/items")
                    .queryParam("search", search)
                    .queryParam("sort", sort)
                    .queryParam("pageNumber", pageNumber)
                    .queryParam("pageSize", pageSize)
                    .build()
                    .toUri();
                return "redirect:%s".formatted(redirectUri.toString());
            });
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


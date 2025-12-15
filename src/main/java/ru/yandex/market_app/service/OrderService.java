package ru.yandex.market_app.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.repository.OrderItemRepository;
import ru.yandex.market_app.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;

    public Flux<Order> getAll() {
        return orderRepo.findAll();
    }

    public Mono<Order> getById(Long id) {
        return orderRepo.findById(id)
            .switchIfEmpty(Mono.error(new ApiServiceException(HttpStatus.NOT_FOUND, "Заказ не найден")));
    }

    @Transactional
    public Mono<Order> buy(List<Item> items) {
        if (items.isEmpty())
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Нет товаров в корзине");

        var newOrder = Order.builder()
            .total(items.stream().mapToInt(item -> item.getPrice() * item.getCartCount()).sum())
            .build();

        return orderRepo.save(newOrder)
            .flatMap(order ->
                orderItemRepo.saveAll(order, items)
                    .collectList()
                    .map(orderItems -> {
                        return order.toBuilder()
                            .items(orderItems)
                            .build();
                    })
            );
    }
}

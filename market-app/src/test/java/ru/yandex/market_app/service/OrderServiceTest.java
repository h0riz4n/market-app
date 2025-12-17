package ru.yandex.market_app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.model.domain.OrderItem;
import ru.yandex.market_app.model.domain.id.OrderItemId;
import ru.yandex.market_app.repository.OrderItemRepository;
import ru.yandex.market_app.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @Mock
    private OrderItemRepository orderItemRepo;

    @InjectMocks
    private OrderService orderService;

    private Item mockItem;
    private Order mockOrder;
    private OrderItem mockOrderItem;

    @BeforeEach
    public void beforeEach() {
        this.mockItem = Item.builder()
            .id(1L)
            .image("image/1.jpg")
            .title("Футбольный мяч")
            .description("Мяч для игры в футбол")
            .cartCount(1)
            .price(100)
            .build();

        this.mockOrder = Order.builder()
            .id(1L)
            .total(mockItem.getPrice())
            .build();
    
        this.mockOrderItem = OrderItem.builder()
            .id(new OrderItemId(mockOrder.getId(), mockItem.getId()))
            .quantity(mockItem.getCartCount())
            .build();
        mockOrder.setItems(List.of(mockOrderItem));
    }

    @AfterEach
    public void afterEach() {
        this.mockItem = null;
        this.mockOrder = null;
    }

    @Test
    public void getAllTest() {
        when(orderRepo.findAll())
            .thenReturn(Flux.just(mockOrder));

        orderService.getAll().collectList().subscribe(orders -> {
            assertTrue(orders.contains(mockOrder));
        });
    }

    @Test
    public void getByIdTest() {
        when(orderRepo.findById(mockOrder.getId()))
            .thenReturn(Mono.just(mockOrder));

        orderService.getById(mockOrder.getId()).subscribe(order -> {
            assertEquals(mockOrder, order);
        });
    }

    @Test
    public void buyTest() {
        assertThrows(
            ApiServiceException.class, 
            () -> orderService.buy(Collections.emptyList()).block()
        );

        mockItem.setCartCount(2);

        when(orderRepo.save(any(Order.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        when(orderItemRepo.saveAll(mockOrder, List.of(mockItem)))
            .thenReturn(Flux.just(mockOrderItem));

        orderService.buy(List.of(mockItem)).subscribe(order -> {
            Integer price = mockItem.getCartCount() * mockItem.getPrice();
            assertEquals(price, order.getTotal());
        });
    }
}

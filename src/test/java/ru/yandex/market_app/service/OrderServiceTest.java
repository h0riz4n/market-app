package ru.yandex.market_app.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.entity.OrderEntity;
import ru.yandex.market_app.model.entity.OrderItemEntity;
import ru.yandex.market_app.model.entity.id.OrderItemId;
import ru.yandex.market_app.repository.OrderRepository;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepo;

    @InjectMocks
    private OrderService orderService;

    private ItemEntity mockItem;
    private OrderEntity mockOrder;

    @BeforeEach
    public void beforeEach() {
        this.mockItem = ItemEntity.builder()
            .id(1L)
            .image("image/1.jpg")
            .title("Футбольный мяч")
            .description("Мяч для игры в футбол")
            .cartCount(1)
            .price(100)
            .build();

        this.mockOrder = OrderEntity.builder()
            .total(mockItem.getPrice())
            .build();
    
        var orderItem = OrderItemEntity.builder()
            .id(new OrderItemId(mockOrder, mockItem))
            .quantity(mockItem.getCartCount())
            .build();
        mockOrder.setItems(List.of(orderItem));
    }

    @AfterEach
    public void afterEach() {
        this.mockItem = null;
        this.mockOrder = null;
    }

    @Test
    public void getAllTest() {
        var expectedOrders = List.of(mockOrder);

        when(orderRepo.findAll())
            .thenReturn(expectedOrders);

        var actualOrders = orderService.getAll();

        assertEquals(expectedOrders, actualOrders);
    }

    @Test
    public void getByIdTest() {
        when(orderRepo.findById(mockOrder.getId()))
            .thenReturn(Optional.of(mockOrder));

        var order = orderService.getById(mockOrder.getId());

        assertEquals(mockOrder, order);
    }

    @Test
    public void buyTest() {
        when(orderRepo.save(any(OrderEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        mockItem.setCartCount(2);
        var order = orderService.buy(List.of(mockItem));

        Integer pr = mockItem.getCartCount() * mockItem.getPrice();
        assertEquals(pr, order.getTotal());
    }
}

package ru.yandex.market_app.repository;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainerTest;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.model.domain.OrderItem;
import ru.yandex.market_app.model.domain.id.OrderItemId;
import ru.yandex.market_app.repository.impl.ItemRepositoryImpl;
import ru.yandex.market_app.repository.impl.OrderItemRepositoryImpl;
import ru.yandex.market_app.repository.impl.OrderRepositoryImpl;
import ru.yandex.market_app.util.DataFactory;

@Tag("integration")
@DataR2dbcTest
@Testcontainers
@Import({ OrderRepositoryImpl.class, ItemRepositoryImpl.class, OrderItemRepositoryImpl.class })
@ImportTestcontainers(DatabaseContainerTest.class)
public class OrderItemRepositoryTest extends DataFactory {

    @BeforeEach
    public void beforeEach() {
        this.mockItem = createItem().block();
        this.mockOrder = null;
    }

    @Test
    public void saveAllTest() {
        Order order = Order.builder()
            .total(mockItem.getPrice())
            .build();
        order = orderRepo.save(order).block();

        mockItem.setCartCount(1);
        orderItemRepo.saveAll(order, List.of(mockItem));
        mockItem.setCartCount(0);

        orderRepo.findById(order.getId()).subscribe(currentOrder -> {
            var expected = OrderItem.builder()
                .id(new OrderItemId(currentOrder.getId(), mockItem.getId()))
                .quantity(1)
                .build();
            assertTrue(currentOrder.getItems().contains(expected));
        });
    }
}

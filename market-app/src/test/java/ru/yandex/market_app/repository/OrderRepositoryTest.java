package ru.yandex.market_app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainerTest;
import ru.yandex.market_app.repository.impl.ItemRepositoryImpl;
import ru.yandex.market_app.repository.impl.OrderItemRepositoryImpl;
import ru.yandex.market_app.repository.impl.OrderRepositoryImpl;
import ru.yandex.market_app.util.DataFactory;

@Tag("integration")
@DataR2dbcTest
@Testcontainers
@Import({ OrderRepositoryImpl.class, ItemRepositoryImpl.class, OrderItemRepositoryImpl.class })
@ImportTestcontainers(DatabaseContainerTest.class)
public class OrderRepositoryTest extends DataFactory {

    @Test
    public void testFindAll() {
        orderRepo.findAll()
            .collectList()
            .subscribe(orders -> {
                assertTrue(orders.contains(mockOrder));
            });
    }

    @Test
    public void testFindById() {
        orderRepo.findById(mockOrder.getId()).subscribe(order -> {
            assertEquals(mockOrder, order);
        });
    }
}

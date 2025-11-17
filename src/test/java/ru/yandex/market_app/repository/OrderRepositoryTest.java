package ru.yandex.market_app.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainer;
import ru.yandex.market_app.util.DataFactory;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@DataJpaTest
@Testcontainers
@ImportTestcontainers(DatabaseContainer.class)
public class OrderRepositoryTest extends DataFactory {

    @Test
    public void testFindAll() {
        var orders = orderRepo.findAll();
        assertTrue(orders.contains(mockOrder));
    }

    @Test
    public void testFindById() {
        var order = orderRepo.findById(mockOrder.getId());

        assertTrue(order.isPresent());
        assertEquals(mockOrder, order.get());
    }
}

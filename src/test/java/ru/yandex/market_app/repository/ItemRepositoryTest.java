package ru.yandex.market_app.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainer;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.filter.ItemFilterModel;
import ru.yandex.market_app.repository.specification.ItemSpecification;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@Tag("integration")
@DataJpaTest
@Testcontainers
@ImportTestcontainers(DatabaseContainer.class)
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepo;

    private ItemEntity mockItem;

    @BeforeEach
    public void beforeEach() {
        this.mockItem = createItem();
    }

    @AfterEach
    public void afterEach() {
        itemRepo.deleteAll();
    }

    @Test
    public void testFindByTitle() {
        var item = itemRepo.findByTitle(mockItem.getTitle());
        assertTrue(item.isPresent());
        assertEquals(mockItem, item.get());
    }

    @Test
    public void testFindAllByCartCountGreaterThan() {
        mockItem.setCartCount(1);
        itemRepo.save(mockItem);

        var items = itemRepo.findAllByCartCountGreaterThan(0);

        assertTrue(items.contains(mockItem));
    }

    @Test
    @Transactional
    public void testUpdateAll() {
        mockItem.setCartCount(1);
        itemRepo.save(mockItem);

        itemRepo.upadteAll();

        var items = itemRepo.findAllByCartCountGreaterThan(0);
        assertFalse(items.contains(mockItem));
    }

    @Test
    public void testFindWithSpecification() {
        var filter = ItemFilterModel.builder()
            .search(mockItem.getTitle().substring(0, 3))
            .build();

        var items = itemRepo.findAll(new ItemSpecification(filter));
        assertTrue(items.contains(mockItem));
    }

    private ItemEntity createItem() {
        var item = ItemEntity.builder()
            .title("Футбольный мяч")
            .description("Большой футбольный мяч для игры на улице")
            .price(100)
            .image("image/1.jpg")
            .build();
        return itemRepo.save(item);
    }
}

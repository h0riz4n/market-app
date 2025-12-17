package ru.yandex.market_app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Testcontainers;

import ru.yandex.market_app.container.DatabaseContainerTest;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.filter.ItemFilterModel;
import ru.yandex.market_app.repository.impl.ItemRepositoryImpl;
import ru.yandex.market_app.repository.specification.ItemSpecification;

@Tag("integration")
@DataR2dbcTest
@Testcontainers
@Import(ItemRepositoryImpl.class)
@ImportTestcontainers(DatabaseContainerTest.class)
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepo;

    private Item mockItem;

    @BeforeEach
    public void beforeEach() {
        this.mockItem = createItem();
    }

    @AfterEach
    public void afterEach() {
        itemRepo.deleteAll().block();
    }

    @Test
    public void testFindByTitle() {
        itemRepo.findByTitle(mockItem.getTitle()).subscribe(item -> {
            assertEquals(mockItem, item);
        });
    }

    @Test
    public void testFindAllByCartCountGreaterThan() {
        mockItem.setCartCount(1);
        itemRepo.updateCartCount(mockItem).block();


        itemRepo.findAllByCartCountGreaterThan(0)
            .collectList()
            .subscribe(items -> {
                assertTrue(items.contains(mockItem));
            });
    }

    @Test
    public void testUpdateAll() {
        mockItem.setCartCount(1);
        itemRepo.updateCartCount(mockItem).block();
        itemRepo.upadteAll().block();

        itemRepo.findAllByCartCountGreaterThan(0)
            .collectList()
            .subscribe(items -> {
                assertFalse(items.contains(mockItem));
            });
    }

    @Test
    public void testFindWithSpecification() {
        var filter = ItemFilterModel.builder()
            .search(mockItem.getTitle().substring(0, 3))
            .build();

        var specification = new ItemSpecification();
        itemRepo.findAll(specification.toCriteria(filter), PageRequest.of(0, 5)).subscribe(page -> {
            assertTrue(page.getContent().contains(mockItem));
        });
    }


    private Item createItem() {
        var item = Item.builder()
            .title("Футбольный мяч")
            .description("Большой футбольный мяч для игры на улице")
            .price(100)
            .image("image/1.jpg")
            .build();
        return itemRepo.save(item).block();
    }
}

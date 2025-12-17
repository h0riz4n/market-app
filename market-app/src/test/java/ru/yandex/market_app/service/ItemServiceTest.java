package ru.yandex.market_app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepo;

    @InjectMocks
    private ItemService itemService;

    private Item mockItem;

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
    }

    @AfterEach
    public void afterEach() {
        this.mockItem = null;
    }

    @Test
    public void getByIdTest() {
        when(itemRepo.findById(mockItem.getId()))
            .thenReturn(Mono.just(mockItem));

        itemService.getById(mockItem.getId()).subscribe(item -> {
            assertEquals(mockItem, item);
        });

        assertDoesNotThrow(() -> itemService.getById(mockItem.getId()).subscribe());
        
        when(itemRepo.findById(anyLong()))
            .thenReturn(Mono.empty());

        assertThrows(ApiServiceException.class, () -> {
            itemService.getById(mockItem.getId()).block();
        });
    }

    @Test
    public void getAllTest() {
        var search = mockItem.getTitle().substring(0, 2);
        var pageable = PageRequest.of(0, 5, Sort.unsorted());
        var expectedItems = List.of(mockItem);

        when(itemRepo.findAll(any(Criteria.class), eq(pageable)))
            .thenReturn(Mono.just(new PageImpl<>(expectedItems, pageable, 1)));

        itemService.getAll(search, ESortType.NO, 0, 5).subscribe(page -> {
            assertEquals(expectedItems, page.getContent());
        });
    }

    @Test
    public void updateCartTest() {
        var newItem = mockItem.toBuilder()
            .cartCount(mockItem.getCartCount() + 1)
            .build();

        when(itemRepo.findById(mockItem.getId()))
            .thenReturn(Mono.just(mockItem));

        when(itemRepo.updateCartCount(mockItem))
            .thenReturn(Mono.just(1L));

        itemService.updateCart(mockItem.getId(), EActionType.PLUS).subscribe(item -> {
            assertEquals(newItem.getCartCount(), item.getCartCount());
        });
    }

    @Test
    public void getAllInCartTest() {
        mockItem.setCartCount(1);

        when(itemRepo.findAllByCartCountGreaterThan(0))
            .thenReturn(Flux.just(mockItem));

        itemService.getAllInCart().collectList().subscribe(items -> {
            assertTrue(items.contains(mockItem));
        });
    }

    @Test
    public void resetCartTest() {
        doAnswer(invoc -> {
            mockItem.setCartCount(0);
            return Mono.just(1L);
        }).when(itemRepo).upadteAll();

        itemService.resetCart().subscribe(val -> {
            assertTrue(mockItem.getCartCount().equals(0));
        });
    }
}

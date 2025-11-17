package ru.yandex.market_app.service;

import static org.hamcrest.Matchers.nullValue;
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
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.entity.ItemEntity;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.model.enums.ESortType;
import ru.yandex.market_app.repository.ItemRepository;
import ru.yandex.market_app.repository.specification.ItemSpecification;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepo;

    @InjectMocks
    private ItemService itemService;

    private ItemEntity mockItem;

    @BeforeEach
    public void beforeEach() {
        this.mockItem = ItemEntity.builder()
            .id(1L)
            .image("image/1.jpg")
            .title("Футбольный мяч")
            .description("Мяч для игры в футбол")
            .cartCount(0)
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
            .thenReturn(Optional.of(mockItem));

        var actualItem = itemService.getById(mockItem.getId());
        assertEquals(mockItem, actualItem);
        assertDoesNotThrow(() -> itemService.getById(mockItem.getId()));

        when(itemRepo.findById(anyLong()))
            .thenReturn(Optional.empty());

        assertThrows(ApiServiceException.class, () -> itemService.getById(1L));
    }

    @Test
    public void getAllTest() {
        var search = mockItem.getTitle().substring(0, 2);
        var pageable = PageRequest.of(0, 5, Sort.unsorted());
        var expectedItems = List.of(mockItem);

        when(itemRepo.findAll(any(ItemSpecification.class), eq(pageable)))
            .thenReturn(new PageImpl<>(expectedItems, pageable, 1));

        var actualItems = itemService.getAll(search, ESortType.NO, 0, 5);

        assertEquals(expectedItems, actualItems.getContent());
    }

    @Test
    public void updateCartTest() {
        var newItem = mockItem.toBuilder()
            .cartCount(mockItem.getCartCount() + 1)
            .build();

        when(itemRepo.findById(mockItem.getId()))
            .thenReturn(Optional.of(mockItem));
        
        when(itemRepo.save(any(ItemEntity.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        var actualItem = itemService.upadteCart(mockItem.getId(), EActionType.PLUS);

        assertEquals(newItem.getCartCount(), actualItem.getCartCount());
    }

    @Test
    public void getAllInCartTest() {
        mockItem.setCartCount(1);

        when(itemRepo.findAllByCartCountGreaterThan(0))
            .thenReturn(List.of(mockItem));

        var itemsInCart = itemService.getAllInCart();
        assertEquals(List.of(mockItem), itemsInCart);
    }

    @Test
    public void resetAllCart() {
        mockItem.setCartCount(1);

        doAnswer(invoc -> {
            mockItem.setCartCount(0);
            return nullValue();
        }).when(itemRepo).upadteAll();

        itemService.resetCart();

        assertTrue(mockItem.getCartCount().equals(0));
    }
}

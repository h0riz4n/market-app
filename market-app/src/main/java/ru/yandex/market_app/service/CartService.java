package ru.yandex.market_app.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.market_app.exception.ApiServiceException;
import ru.yandex.market_app.model.domain.Cart;
import ru.yandex.market_app.model.domain.id.CartId;
import ru.yandex.market_app.model.enums.EActionType;
import ru.yandex.market_app.repository.CartRepository;

@Service
@RequiredArgsConstructor
public class CartService {

    private final UserService userService;

    private final TransactionalOperator transactionalOperator;
    private final CartRepository cartRepo;

    public Flux<Cart> getCart() {
        return userService.getUserIdFromSecurityContext()
            .flatMapMany(cartRepo::findAllByUserId);
    }

    public Mono<Long> getCartCountByItemId(Long itemId) {
        return userService.getUserIdFromSecurityContext()
            .flatMap(userId -> {
                return cartRepo.findByUserIdAndItemId(userId, itemId)
                    .map(Cart::getCount)
                    .defaultIfEmpty(0L);
            });
    }

    public Mono<Cart> updateCart(Long itemId, EActionType action) {
        return userService.getUserIdFromSecurityContext()
            .flatMap(userId -> {
                return inTransaction(() -> {
                    return cartRepo.findByUserIdAndItemId(userId, itemId)
                        .flatMap(cart -> updateCartCount(cart, action))
                        .switchIfEmpty(handleEmptyCart(userId, itemId, action));
                });
            });
    }

    public Mono<Long> resetCart() {
        return userService.getUserIdFromSecurityContext()
            .flatMap(cartRepo::deleteAllByUserId);
    }

    public Flux<Cart> getAllByItemIdIn(List<Long> itemIds) {
        return userService.getUserIdFromSecurityContext()
            .flatMapMany(userId -> cartRepo.findAllByUserIdAndItemIdIn(userId, itemIds));
    }

    private Mono<Cart> handleEmptyCart(UUID userId, Long itemId, EActionType action) {
        var cart = Cart.builder()
            .id(new CartId(itemId, userId))
            .count(1L)
            .build();

        if (EActionType.PLUS.equals(action)) {
            return cartRepo.save(cart);
        } else {
            return Mono.error(new ApiServiceException(HttpStatus.BAD_REQUEST, "Товара и так нет в корзине"));
        }
    }

    private Mono<Cart> updateCartCount(Cart cart, EActionType action) {
        return switch (action) {
            case PLUS -> incrementInCart(cart);
            case MINUS -> decrementInCart(cart);
            case DELETE -> deleteInCart(cart);
        };
    }

    private Mono<Cart> incrementInCart(Cart cart) {
        cart.setCount(cart.getCount() + 1);
        return cartRepo.updateCount(cart, cart.getCount())
            .thenReturn(cart);
    }

    private Mono<Cart> decrementInCart(Cart cart) {
        if (cart.getCount() <= 1) {
            return deleteInCart(cart);
        } else {
            cart.setCount(cart.getCount() - 1);
            return cartRepo.updateCount(cart, cart.getCount())
                .thenReturn(cart);
        }
    }

    private Mono<Cart> deleteInCart(Cart cart) {
        var cartId = cart.getId();
        cart.setCount(0L);
        return cartRepo.deleteByUserIdAndItemId(cartId.userId(), cartId.itemId())
            .thenReturn(cart);
    }

    private <T> Mono<T> inTransaction(Supplier<Mono<T>> supplier) {
        return transactionalOperator.transactional(Mono.defer(supplier));
    }
}

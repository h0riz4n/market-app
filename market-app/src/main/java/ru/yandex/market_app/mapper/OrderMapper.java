package ru.yandex.market_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.model.domain.Order;
import ru.yandex.market_app.model.domain.OrderItem;
import ru.yandex.market_app.model.dto.ItemDto;
import ru.yandex.market_app.model.dto.OrderDto;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@RequiredArgsConstructor
public abstract class OrderMapper {

    @Mapping(target = "items", source = "entity.items", qualifiedByName = "toItemDto")
    @Mapping(target = "totalSum", source = "entity.total")
    public abstract OrderDto toDto(Order entity);

    public abstract List<OrderDto> toDtos(List<Order> entities);

    @Named("toItemDto") 
    public List<ItemDto> toItemDto(List<OrderItem> orderItems) {
        return orderItems.stream()
            .map(orderItem -> {
                var item = orderItem.getItem();
                return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getPrice(), item.getImage(), orderItem.getQuantity());
            })
            .toList();
    }
}


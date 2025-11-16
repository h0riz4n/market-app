package ru.yandex.market_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import lombok.RequiredArgsConstructor;
import ru.yandex.market_app.model.dto.ItemDto;
import ru.yandex.market_app.model.dto.OrderDto;
import ru.yandex.market_app.model.entity.OrderEntity;
import ru.yandex.market_app.model.entity.OrderItemEntity;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
@RequiredArgsConstructor
public abstract class OrderMapper {

    @Mapping(target = "items", source = "entity.items", qualifiedByName = "toItemDto")
    @Mapping(target = "totalSum", source = "entity.total")
    public abstract OrderDto toDto(OrderEntity entity);

    public abstract List<OrderDto> toDtos(List<OrderEntity> entities);

    @Named("toItemDto")
    public List<ItemDto> toItemDto(List<OrderItemEntity> orderItems) {
        return orderItems.stream()
            .map(orderItem ->{
                var item = orderItem.getId().getItem();
                return new ItemDto(item.getId(), item.getTitle(), item.getDescription(), item.getPrice(), item.getImage(), orderItem.getQuantity());
            })
            .toList();
    }
}

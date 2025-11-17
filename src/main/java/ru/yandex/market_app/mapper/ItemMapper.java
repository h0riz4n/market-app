package ru.yandex.market_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.market_app.model.dto.ItemDto;
import ru.yandex.market_app.model.entity.ItemEntity;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class ItemMapper {

    @Mapping(target = "imgPath", source = "entity.image")
    @Mapping(target = "count", source = "entity.cartCount")
    public abstract ItemDto toDto(ItemEntity entity);

    public abstract List<ItemDto> toDtos(List<ItemEntity> items);
}

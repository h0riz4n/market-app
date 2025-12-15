package ru.yandex.market_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.market_app.model.domain.Item;
import ru.yandex.market_app.model.dto.ItemDto;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class ItemMapper {

    @Mapping(target = "imgPath", source = "entity.image")
    @Mapping(target = "count", source = "entity.cartCount")
    public abstract ItemDto toDto(Item entity);

    public abstract List<ItemDto> toDtos(List<Item> items);
}

package ru.yandex.market_app.model.dto;

import java.util.List;

public record OrderDto(
    Long id,

    List<ItemDto> items,
    
    Integer totalSum
) {

}

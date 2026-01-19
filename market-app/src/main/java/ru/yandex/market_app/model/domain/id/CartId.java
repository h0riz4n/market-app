package ru.yandex.market_app.model.domain.id;

import java.util.UUID;

public record CartId(

    Long itemId,

    UUID userId
) {

}

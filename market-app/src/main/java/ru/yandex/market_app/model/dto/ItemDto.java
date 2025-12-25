package ru.yandex.market_app.model.dto;

public record ItemDto (

    Long id,

    String title,

    String description,

    Integer price,

    String imgPath,

    Integer count
) { }

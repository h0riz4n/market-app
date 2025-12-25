package ru.yandex.payment_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.model.BalanceResponse;
import ru.yandex.payment_service.model.domain.Balance;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class BalanceMapper {

    public abstract BalanceResponse toDto(Balance entity);
}

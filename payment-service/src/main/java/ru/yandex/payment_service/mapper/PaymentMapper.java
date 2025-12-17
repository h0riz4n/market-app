package ru.yandex.payment_service.mapper;


import ru.yandex.domain.PaymentRequest;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.payment_service.model.domain.Payment;


@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class PaymentMapper {

    public abstract PaymentRequest toDto(Payment entity);
}

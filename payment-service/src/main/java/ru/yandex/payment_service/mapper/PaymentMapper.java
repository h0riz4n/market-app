package ru.yandex.payment_service.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import ru.yandex.model.PaymentResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.payment_service.model.domain.Payment;


@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class PaymentMapper {

    @Mapping(target = "paymentDateTime", source = "entity.paymentDateTime", qualifiedByName = "toOffsetDateTime")
    @Mapping(target = "transactionId", source = "entity.id")
    public abstract PaymentResponse toDto(Payment entity);

    @Named("toOffsetDateTime")
    public OffsetDateTime toOffsetDateTime(LocalDateTime time) {
        return time.atOffset(ZoneOffset.UTC);
    }
}

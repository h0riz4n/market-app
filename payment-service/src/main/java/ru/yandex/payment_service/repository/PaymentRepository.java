package ru.yandex.payment_service.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import ru.yandex.payment_service.model.domain.Payment;

@Repository
public interface PaymentRepository extends R2dbcRepository<Payment, Long> {

}

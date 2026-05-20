package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.PaymentMethod;

public interface PaymentMethodRepositoryPort {

    List<PaymentMethod> findAll();

    Optional<PaymentMethod> findActive();

    Optional<PaymentMethod> findInactive();

    Optional<PaymentMethod> findById(Long id);

    PaymentMethod save(PaymentMethod paymentMethod);
}

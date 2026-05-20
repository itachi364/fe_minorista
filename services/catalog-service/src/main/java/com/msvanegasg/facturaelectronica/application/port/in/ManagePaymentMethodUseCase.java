package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.PaymentMethodCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.PaymentMethod;

public interface ManagePaymentMethodUseCase {

    List<PaymentMethod> findAll();

    PaymentMethod findActive();

    PaymentMethod findInactive();

    PaymentMethod findById(Long id);

    PaymentMethod create(PaymentMethodCommand command);

    PaymentMethod update(Long id, PaymentMethodCommand command);

    void disable(Long id);

    void enable(Long id);
}

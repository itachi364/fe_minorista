package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.PaymentMethodCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManagePaymentMethodUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.PaymentMethodRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.PaymentMethod;
import com.msvanegasg.facturaelectronica.exception.MetodoPagoNotFoundException;

public class PaymentMethodManagementService implements ManagePaymentMethodUseCase {

    private final PaymentMethodRepositoryPort paymentMethodRepository;

    public PaymentMethodManagementService(PaymentMethodRepositoryPort paymentMethodRepository) {
        this.paymentMethodRepository = Objects.requireNonNull(paymentMethodRepository);
    }

    @Override
    public List<PaymentMethod> findAll() {
        return paymentMethodRepository.findAll();
    }

    @Override
    public PaymentMethod findActive() {
        return paymentMethodRepository.findActive()
                .orElseThrow(() -> new MetodoPagoNotFoundException(null));
    }

    @Override
    public PaymentMethod findInactive() {
        return paymentMethodRepository.findInactive()
                .orElseThrow(() -> new MetodoPagoNotFoundException(null));
    }

    @Override
    public PaymentMethod findById(Long id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new MetodoPagoNotFoundException(id));
    }

    @Override
    public PaymentMethod create(PaymentMethodCommand command) {
        Objects.requireNonNull(command, "command is required");
        return paymentMethodRepository.save(PaymentMethod.create(command.name(), command.description()));
    }

    @Override
    public PaymentMethod update(Long id, PaymentMethodCommand command) {
        Objects.requireNonNull(command, "command is required");
        PaymentMethod existing = findById(id);
        return paymentMethodRepository.save(existing.update(command.name(), command.description()));
    }

    @Override
    public void disable(Long id) {
        PaymentMethod existing = findById(id);
        paymentMethodRepository.save(existing.disable());
    }

    @Override
    public void enable(Long id) {
        PaymentMethod existing = findById(id);
        if (!existing.active()) {
            paymentMethodRepository.save(existing.enable());
        }
    }
}

package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.PaymentMethodRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.PaymentMethod;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.PaymentMethodJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.PaymentMethodJpaRepository;

@Component
public class PaymentMethodPersistenceAdapter implements PaymentMethodRepositoryPort {

    private final PaymentMethodJpaRepository paymentMethodJpaRepository;

    public PaymentMethodPersistenceAdapter(PaymentMethodJpaRepository paymentMethodJpaRepository) {
        this.paymentMethodJpaRepository = paymentMethodJpaRepository;
    }

    @Override
    public List<PaymentMethod> findAll() {
        return paymentMethodJpaRepository.findAll().stream()
                .map(PaymentMethodPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<PaymentMethod> findActive() {
        return Optional.ofNullable(paymentMethodJpaRepository.findByActivoTrue())
                .map(PaymentMethodPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<PaymentMethod> findInactive() {
        return Optional.ofNullable(paymentMethodJpaRepository.findByActivoFalse())
                .map(PaymentMethodPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<PaymentMethod> findById(Long id) {
        return paymentMethodJpaRepository.findById(id)
                .map(PaymentMethodPersistenceAdapter::toDomain);
    }

    @Override
    public PaymentMethod save(PaymentMethod paymentMethod) {
        PaymentMethodJpaEntity saved = paymentMethodJpaRepository.save(toEntity(paymentMethod));
        return toDomain(saved);
    }

    private static PaymentMethod toDomain(PaymentMethodJpaEntity entity) {
        return PaymentMethod.restore(
                entity.getIdMetodoPago(),
                entity.getNombre(),
                entity.getDescripcion(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static PaymentMethodJpaEntity toEntity(PaymentMethod paymentMethod) {
        return PaymentMethodJpaEntity.builder()
                .idMetodoPago(paymentMethod.id())
                .nombre(paymentMethod.name())
                .descripcion(paymentMethod.description())
                .activo(paymentMethod.active())
                .build();
    }
}

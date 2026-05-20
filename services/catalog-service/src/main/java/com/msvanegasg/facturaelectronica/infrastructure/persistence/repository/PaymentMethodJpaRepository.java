package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.PaymentMethodJpaEntity;

@Repository
public interface PaymentMethodJpaRepository extends JpaRepository<PaymentMethodJpaEntity, Long> {

    PaymentMethodJpaEntity findByActivoFalse();

    PaymentMethodJpaEntity findByActivoTrue();

    List<PaymentMethodJpaEntity> findByNombreContainingIgnoreCase(String nombre);
}

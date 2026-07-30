package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;

public interface TaxRepositoryPort {

    List<Tax> findAll();

    Optional<Tax> findActive();

    Optional<Tax> findInactive();

    Optional<Tax> findById(Long id);

    Optional<Tax> findByPercentage(BigDecimal percentage);

    Optional<Tax> findByType(String type);

    Tax save(Tax tax);
}

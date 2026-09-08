package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.FiscalParameterJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.FiscalParameterJpaRepository;

@Component
public class FiscalParameterPersistenceAdapter implements FiscalParameterRepositoryPort {
    private final FiscalParameterJpaRepository repository;

    public FiscalParameterPersistenceAdapter(FiscalParameterJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<FiscalParameter> findEffective(String code, LocalDate date) {
        return repository.findEffective(code, date).stream().findFirst().map(FiscalParameterPersistenceAdapter::toDomain);
    }

    @Override
    public List<FiscalParameter> findAll() {
        return repository.findAll().stream().map(FiscalParameterPersistenceAdapter::toDomain)
                .sorted(Comparator.comparing(FiscalParameter::code).thenComparing(FiscalParameter::validFrom).reversed())
                .toList();
    }

    private static FiscalParameter toDomain(FiscalParameterJpaEntity entity) {
        return new FiscalParameter(entity.getId(), entity.getCode(), entity.getVersion(), entity.getValue(),
                entity.getValidFrom(), entity.getValidTo(), entity.getLegalReference(), entity.getSourceUrl(),
                Boolean.TRUE.equals(entity.getPublished()));
    }
}

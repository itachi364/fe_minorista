package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.TaxRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CountryJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.TaxJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.CountryJpaRepository;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.TaxJpaRepository;
import com.msvanegasg.facturaelectronica.exception.PaisNotFoundException;

@Component
public class TaxPersistenceAdapter implements TaxRepositoryPort {

    private final TaxJpaRepository taxRepository;
    private final CountryJpaRepository countryRepository;

    public TaxPersistenceAdapter(TaxJpaRepository taxRepository, CountryJpaRepository countryRepository) {
        this.taxRepository = taxRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public List<Tax> findAll() {
        return taxRepository.findAll().stream()
                .map(TaxPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Tax> findActive() {
        return Optional.ofNullable(taxRepository.findByActivoTrue())
                .map(TaxPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Tax> findInactive() {
        return Optional.ofNullable(taxRepository.findByActivoFalse())
                .map(TaxPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Tax> findById(Long id) {
        return taxRepository.findById(id)
                .map(TaxPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Tax> findByPercentage(BigDecimal percentage) {
        return taxRepository.findByPorcentaje(percentage)
                .map(TaxPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Tax> findByType(String type) {
        return taxRepository.findByTipo(type)
                .map(TaxPersistenceAdapter::toDomain);
    }

    @Override
    public Tax save(Tax tax) {
        TaxJpaEntity saved = taxRepository.save(toEntity(tax));
        return toDomain(saved);
    }

    private TaxJpaEntity toEntity(Tax tax) {
        CountryJpaEntity country = countryRepository.findById(tax.country().code())
                .orElseThrow(() -> new PaisNotFoundException(tax.country().code()));
        return TaxJpaEntity.builder()
                .idImpuesto(tax.id())
                .nombre(tax.name())
                .porcentaje(tax.percentage())
                .tipo(tax.type())
                .pais(country)
                .activo(tax.active())
                .descripcion(tax.description())
                .build();
    }

    private static Tax toDomain(TaxJpaEntity entity) {
        return Tax.restore(
                entity.getIdImpuesto(),
                entity.getNombre(),
                entity.getPorcentaje(),
                entity.getTipo(),
                toCountry(entity.getPais()),
                entity.getDescripcion(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static Country toCountry(CountryJpaEntity entity) {
        return Country.restore(
                entity.getCodigoPais(),
                entity.getNombre(),
                entity.getMoneda(),
                Boolean.TRUE.equals(entity.getActivo()));
    }
}

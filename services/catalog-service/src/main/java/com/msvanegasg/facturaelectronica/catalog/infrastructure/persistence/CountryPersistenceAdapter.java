package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.CountryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CountryJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.CountryJpaRepository;

@Component
public class CountryPersistenceAdapter implements CountryRepositoryPort {

    private final CountryJpaRepository countryJpaRepository;

    public CountryPersistenceAdapter(CountryJpaRepository countryJpaRepository) {
        this.countryJpaRepository = countryJpaRepository;
    }

    @Override
    public List<Country> findAll() {
        return countryJpaRepository.findAll().stream()
                .map(CountryPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Country> findActive() {
        return countryJpaRepository.findByActivoTrue().stream()
                .map(CountryPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Country> findInactive() {
        return countryJpaRepository.findByActivoFalse().stream()
                .map(CountryPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Country> findByCode(String code) {
        return countryJpaRepository.findById(code)
                .map(CountryPersistenceAdapter::toDomain);
    }

    @Override
    public Country save(Country country) {
        CountryJpaEntity saved = countryJpaRepository.save(toEntity(country));
        return toDomain(saved);
    }

    private static Country toDomain(CountryJpaEntity entity) {
        return Country.restore(
                entity.getCodigoPais(),
                entity.getNombre(),
                entity.getMoneda(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static CountryJpaEntity toEntity(Country country) {
        return CountryJpaEntity.builder()
                .codigoPais(country.code())
                .nombre(country.name())
                .moneda(country.currency())
                .activo(country.active())
                .build();
    }
}

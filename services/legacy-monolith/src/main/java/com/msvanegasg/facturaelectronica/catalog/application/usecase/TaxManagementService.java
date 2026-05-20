package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.TaxCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageTaxUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CountryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.TaxRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;
import com.msvanegasg.facturaelectronica.exception.PaisNotFoundException;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoInactivoException;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.impuesto.TipoImpuestoNotFoundException;

public class TaxManagementService implements ManageTaxUseCase {

    private final TaxRepositoryPort taxRepository;
    private final CountryRepositoryPort countryRepository;

    public TaxManagementService(TaxRepositoryPort taxRepository, CountryRepositoryPort countryRepository) {
        this.taxRepository = Objects.requireNonNull(taxRepository);
        this.countryRepository = Objects.requireNonNull(countryRepository);
    }

    @Override
    public List<Tax> findAll() {
        return taxRepository.findAll();
    }

    @Override
    public Tax findActive() {
        return taxRepository.findActive()
                .orElseThrow(() -> new ImpuestoNotFoundException("No existe Impuesto activo"));
    }

    @Override
    public Tax findInactive() {
        return taxRepository.findInactive()
                .orElseThrow(() -> new ImpuestoNotFoundException("No existe Impuesto inactivo"));
    }

    @Override
    public Tax findById(Long id) {
        return taxRepository.findById(id)
                .orElseThrow(() -> new ImpuestoNotFoundException(id));
    }

    @Override
    public Tax findByPercentage(BigDecimal percentage) {
        return taxRepository.findByPercentage(percentage)
                .orElseThrow(() -> new IllegalArgumentException("No existe Impuesto con el porcentaje: " + percentage));
    }

    @Override
    public Tax findByType(String type) {
        return taxRepository.findByType(type)
                .orElseThrow(() -> new TipoImpuestoNotFoundException(type));
    }

    @Override
    public Tax create(TaxCommand command) {
        Objects.requireNonNull(command, "command is required");
        Country country = findCountry(command.countryCode());
        return taxRepository.save(Tax.create(
                command.name(),
                command.percentage(),
                command.type(),
                country,
                command.description()));
    }

    @Override
    public Tax update(Long id, TaxCommand command) {
        Objects.requireNonNull(command, "command is required");
        Tax existing = findById(id);
        if (!existing.active()) {
            throw new ImpuestoInactivoException(id);
        }
        Country country = findCountry(command.countryCode());
        return taxRepository.save(existing.update(
                command.name(),
                command.percentage(),
                command.type(),
                country,
                command.description()));
    }

    @Override
    public void disable(Long id) {
        Tax existing = findById(id);
        taxRepository.save(existing.disable());
    }

    @Override
    public void enable(Long id) {
        Tax existing = findById(id);
        if (!existing.active()) {
            taxRepository.save(existing.enable());
        }
    }

    private Country findCountry(String countryCode) {
        return countryRepository.findByCode(countryCode)
                .orElseThrow(() -> new PaisNotFoundException(countryCode));
    }
}

package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CountryCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCountryUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CountryRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.exception.PaisNotFoundException;

public class CountryManagementService implements ManageCountryUseCase {

    private final CountryRepositoryPort countryRepository;

    public CountryManagementService(CountryRepositoryPort countryRepository) {
        this.countryRepository = Objects.requireNonNull(countryRepository);
    }

    @Override
    public List<Country> findAll() {
        return countryRepository.findAll();
    }

    @Override
    public List<Country> findActive() {
        return countryRepository.findActive();
    }

    @Override
    public List<Country> findInactive() {
        return countryRepository.findInactive();
    }

    @Override
    public Country findByCode(String code) {
        return countryRepository.findByCode(code)
                .orElseThrow(() -> new PaisNotFoundException(code));
    }

    @Override
    public Country create(CountryCommand command) {
        Objects.requireNonNull(command, "command is required");
        return countryRepository.save(Country.create(command.code(), command.name(), command.currency()));
    }

    @Override
    public Country update(String code, CountryCommand command) {
        Objects.requireNonNull(command, "command is required");
        Country existing = findByCode(code);
        return countryRepository.save(existing.update(command.name(), command.currency()));
    }

    @Override
    public void disable(String code) {
        Country existing = findByCode(code);
        countryRepository.save(existing.disable());
    }

    @Override
    public void enable(String code) {
        Country existing = findByCode(code);
        if (!existing.active()) {
            countryRepository.save(existing.enable());
        }
    }
}

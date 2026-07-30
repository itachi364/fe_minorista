package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;

public interface CountryRepositoryPort {

    List<Country> findAll();

    List<Country> findActive();

    List<Country> findInactive();

    Optional<Country> findByCode(String code);

    Country save(Country country);
}

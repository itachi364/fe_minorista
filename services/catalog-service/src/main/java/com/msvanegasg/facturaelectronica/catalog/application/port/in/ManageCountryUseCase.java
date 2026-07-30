package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CountryCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;

public interface ManageCountryUseCase {

    List<Country> findAll();

    List<Country> findActive();

    List<Country> findInactive();

    Country findByCode(String code);

    Country create(CountryCommand command);

    Country update(String code, CountryCommand command);

    void disable(String code);

    void enable(String code);
}

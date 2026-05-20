package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.math.BigDecimal;
import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.TaxCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;

public interface ManageTaxUseCase {

    List<Tax> findAll();

    Tax findActive();

    Tax findInactive();

    Tax findById(Long id);

    Tax findByPercentage(BigDecimal percentage);

    Tax findByType(String type);

    Tax create(TaxCommand command);

    Tax update(Long id, TaxCommand command);

    void disable(Long id);

    void enable(Long id);
}

package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;

public interface FiscalParameterRepositoryPort {
    Optional<FiscalParameter> findEffective(String code, LocalDate date);
    List<FiscalParameter> findAll();
}

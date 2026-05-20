package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.catalog.domain.model.Parameter;

public interface ParameterRepositoryPort {

    List<Parameter> findAll();

    List<Parameter> findActive();

    List<Parameter> findInactive();

    Optional<Parameter> findById(Long id);

    Parameter save(Parameter parameter);
}

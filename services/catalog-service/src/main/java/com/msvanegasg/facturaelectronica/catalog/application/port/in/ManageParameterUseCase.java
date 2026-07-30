package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ParameterCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Parameter;

public interface ManageParameterUseCase {

    List<Parameter> findAll();

    List<Parameter> findActive();

    List<Parameter> findInactive();

    Parameter findById(Long id);

    Parameter create(ParameterCommand command);

    Parameter update(Long id, ParameterCommand command);

    void disable(Long id);

    void enable(Long id);
}

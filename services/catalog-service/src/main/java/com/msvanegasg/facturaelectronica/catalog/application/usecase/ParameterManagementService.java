package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ParameterCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageParameterUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.ParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Parameter;
import com.msvanegasg.facturaelectronica.exception.ParametroNotFoundException;

public class ParameterManagementService implements ManageParameterUseCase {

    private final ParameterRepositoryPort parameterRepository;

    public ParameterManagementService(ParameterRepositoryPort parameterRepository) {
        this.parameterRepository = Objects.requireNonNull(parameterRepository);
    }

    @Override
    public List<Parameter> findAll() {
        return parameterRepository.findAll();
    }

    @Override
    public List<Parameter> findActive() {
        return parameterRepository.findActive();
    }

    @Override
    public List<Parameter> findInactive() {
        return parameterRepository.findInactive();
    }

    @Override
    public Parameter findById(Long id) {
        return parameterRepository.findById(id)
                .orElseThrow(() -> new ParametroNotFoundException(id));
    }

    @Override
    public Parameter create(ParameterCommand command) {
        Objects.requireNonNull(command, "command is required");
        return parameterRepository.save(Parameter.create(command.key(), command.value(), command.description()));
    }

    @Override
    public Parameter update(Long id, ParameterCommand command) {
        Objects.requireNonNull(command, "command is required");
        Parameter existing = findById(id);
        return parameterRepository.save(existing.update(command.key(), command.value(), command.description()));
    }

    @Override
    public void disable(Long id) {
        Parameter existing = findById(id);
        parameterRepository.save(existing.disable());
    }

    @Override
    public void enable(Long id) {
        Parameter existing = findById(id);
        if (!existing.active()) {
            parameterRepository.save(existing.enable());
        }
    }
}

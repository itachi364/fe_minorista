package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.ParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Parameter;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ParameterJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.ParameterJpaRepository;

@Component
public class ParameterPersistenceAdapter implements ParameterRepositoryPort {

    private final ParameterJpaRepository parameterRepository;

    public ParameterPersistenceAdapter(ParameterJpaRepository parameterRepository) {
        this.parameterRepository = parameterRepository;
    }

    @Override
    public List<Parameter> findAll() {
        return parameterRepository.findAll().stream()
                .map(ParameterPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Parameter> findActive() {
        return parameterRepository.findByActivoTrue().stream()
                .map(ParameterPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Parameter> findInactive() {
        return parameterRepository.findByActivoFalse().stream()
                .map(ParameterPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Parameter> findById(Long id) {
        return parameterRepository.findById(id)
                .map(ParameterPersistenceAdapter::toDomain);
    }

    @Override
    public Parameter save(Parameter parameter) {
        ParameterJpaEntity saved = parameterRepository.save(toEntity(parameter));
        return toDomain(saved);
    }

    private static Parameter toDomain(ParameterJpaEntity entity) {
        return Parameter.restore(
                entity.getIdParametro(),
                entity.getClave(),
                entity.getValor(),
                entity.getDescripcion(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static ParameterJpaEntity toEntity(Parameter parameter) {
        return ParameterJpaEntity.builder()
                .idParametro(parameter.id())
                .clave(parameter.key())
                .valor(parameter.value())
                .descripcion(parameter.description())
                .activo(parameter.active())
                .build();
    }
}

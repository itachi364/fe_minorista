package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.ExpenseTypeRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ExpenseTypeJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.ExpenseTypeJpaRepository;

@Component
public class ExpenseTypePersistenceAdapter implements ExpenseTypeRepositoryPort {

    private final ExpenseTypeJpaRepository expenseTypeRepository;

    public ExpenseTypePersistenceAdapter(ExpenseTypeJpaRepository expenseTypeRepository) {
        this.expenseTypeRepository = expenseTypeRepository;
    }

    @Override
    public List<ExpenseType> findAll() {
        return expenseTypeRepository.findAll().stream()
                .map(ExpenseTypePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ExpenseType> findActive() {
        return expenseTypeRepository.findByActivoTrue().stream()
                .map(ExpenseTypePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ExpenseType> findInactive() {
        return expenseTypeRepository.findByActivoFalse().stream()
                .map(ExpenseTypePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<ExpenseType> findById(Long id) {
        return expenseTypeRepository.findById(id)
                .map(ExpenseTypePersistenceAdapter::toDomain);
    }

    @Override
    public ExpenseType save(ExpenseType expenseType) {
        ExpenseTypeJpaEntity saved = expenseTypeRepository.save(toEntity(expenseType));
        return toDomain(saved);
    }

    private static ExpenseType toDomain(ExpenseTypeJpaEntity entity) {
        return ExpenseType.restore(
                entity.getIdTipoGasto(),
                entity.getNombre(),
                entity.getDescripcion(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static ExpenseTypeJpaEntity toEntity(ExpenseType expenseType) {
        return ExpenseTypeJpaEntity.builder()
                .idTipoGasto(expenseType.id())
                .nombre(expenseType.name())
                .descripcion(expenseType.description())
                .activo(expenseType.active())
                .build();
    }
}

package com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.expenses.infrastructure.persistence.entity.ExpenseJpaEntity;

public interface ExpenseJpaRepository extends JpaRepository<ExpenseJpaEntity, Long> {

    List<ExpenseJpaEntity> findByTipoGastoIdTipoGasto(Long idTipoGasto);

    List<ExpenseJpaEntity> findByMetodoPagoIdMetodoPago(Long idMetodoPago);

    ExpenseJpaEntity findByDescripcionContainingIgnoreCase(String descripcion);

    List<ExpenseJpaEntity> findByActivo(Boolean activo);

    List<ExpenseJpaEntity> findByEstado(Estado estado);
}

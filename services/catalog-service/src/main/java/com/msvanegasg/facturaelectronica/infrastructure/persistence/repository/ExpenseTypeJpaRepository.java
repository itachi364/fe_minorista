package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.ExpenseTypeJpaEntity;

@Repository
public interface ExpenseTypeJpaRepository extends JpaRepository<ExpenseTypeJpaEntity, Long> {

    List<ExpenseTypeJpaEntity> findByActivoTrue();

    List<ExpenseTypeJpaEntity> findByActivoFalse();

    boolean existsByNombreIgnoreCase(String nombre);
}

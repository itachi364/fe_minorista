package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.TaxJpaEntity;

@Repository
public interface TaxJpaRepository extends JpaRepository<TaxJpaEntity, Long> {

    TaxJpaEntity findByActivoTrue();

    TaxJpaEntity findByActivoFalse();

    List<TaxJpaEntity> findByNombreContainingIgnoreCase(String nombre);

    Optional<TaxJpaEntity> findByTipo(String tipo);

    Optional<TaxJpaEntity> findByPorcentaje(BigDecimal porcentaje);
}

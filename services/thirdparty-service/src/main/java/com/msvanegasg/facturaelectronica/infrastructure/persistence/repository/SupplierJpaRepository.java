package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity.SupplierJpaEntity;

public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, Long> {

    Optional<SupplierJpaEntity> findByNumeroDocumentoAndIdTipoDocumento(Long numeroDocumento, Long idTipoDocumento);

    SupplierJpaEntity findByNombreContainingIgnoreCase(String nombre);

    List<SupplierJpaEntity> findByActivoTrue();

    List<SupplierJpaEntity> findByActivoFalse();

    boolean existsByNumeroDocumento(Long numeroDocumento);
}

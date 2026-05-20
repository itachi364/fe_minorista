package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity.CustomerJpaEntity;

public interface CustomerJpaRepository extends JpaRepository<CustomerJpaEntity, Long> {

    Optional<CustomerJpaEntity> findByIdTipoDocumentoAndNumeroDocumento(Long idTipoDocumento, Long numeroDocumento);

    List<CustomerJpaEntity> findAllByActivo(Boolean activo);

    List<CustomerJpaEntity> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByNumeroDocumentoAndIdTipoDocumento(Long numeroDocumento, Long idTipoDocumento);
}

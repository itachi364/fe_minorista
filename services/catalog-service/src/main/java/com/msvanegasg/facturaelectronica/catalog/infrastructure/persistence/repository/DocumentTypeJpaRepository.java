package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.DocumentTypeJpaEntity;

@Repository
public interface DocumentTypeJpaRepository extends JpaRepository<DocumentTypeJpaEntity, Integer> {

    List<DocumentTypeJpaEntity> findByActivoTrue();

    List<DocumentTypeJpaEntity> findByActivoFalse();

    boolean existsByNombreIgnoreCase(String nombre);
}

package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CatalogItemJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CatalogItemJpaId;

@Repository
public interface CatalogItemJpaRepository extends JpaRepository<CatalogItemJpaEntity, CatalogItemJpaId> {

    List<CatalogItemJpaEntity> findByIdCatalogCodeAndActiveTrueOrderBySortOrderAscLabelAsc(String catalogCode);

    List<CatalogItemJpaEntity> findByIdCatalogCodeOrderBySortOrderAscLabelAsc(String catalogCode);
}

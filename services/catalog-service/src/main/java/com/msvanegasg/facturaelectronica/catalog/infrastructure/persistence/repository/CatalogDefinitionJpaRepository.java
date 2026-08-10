package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CatalogDefinitionJpaEntity;

@Repository
public interface CatalogDefinitionJpaRepository extends JpaRepository<CatalogDefinitionJpaEntity, String> {

    List<CatalogDefinitionJpaEntity> findByActiveTrueOrderBySortOrderAscLabelAsc();
}

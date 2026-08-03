package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.PermissionCatalogJpaEntity;

public interface PermissionCatalogJpaRepository extends JpaRepository<PermissionCatalogJpaEntity, PermissionCode> {

    List<PermissionCatalogJpaEntity> findByActiveTrueOrderByScopeAscModuleAscCodeAsc();
}

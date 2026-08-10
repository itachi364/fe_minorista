package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CompanyCatalogItemSettingJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CompanyCatalogItemSettingJpaId;

@Repository
public interface CompanyCatalogItemSettingJpaRepository
        extends JpaRepository<CompanyCatalogItemSettingJpaEntity, CompanyCatalogItemSettingJpaId> {
}

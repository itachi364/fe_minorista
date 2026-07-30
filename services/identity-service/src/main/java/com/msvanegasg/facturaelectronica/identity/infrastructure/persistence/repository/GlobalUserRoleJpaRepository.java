package com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.GlobalUserRoleId;
import com.msvanegasg.facturaelectronica.identity.infrastructure.persistence.entity.GlobalUserRoleJpaEntity;

public interface GlobalUserRoleJpaRepository extends JpaRepository<GlobalUserRoleJpaEntity, GlobalUserRoleId> {

    List<GlobalUserRoleJpaEntity> findByUserId(UUID userId);

    boolean existsByUserIdAndRoleCode(UUID userId, GlobalRoleCode roleCode);
}
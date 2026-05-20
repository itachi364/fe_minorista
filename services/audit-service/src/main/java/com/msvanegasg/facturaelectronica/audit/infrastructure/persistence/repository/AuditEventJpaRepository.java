package com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.entity.AuditEventJpaEntity;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, UUID> {

    List<AuditEventJpaEntity> findByCompanyIdOrderByOccurredAtDesc(UUID companyId);
}

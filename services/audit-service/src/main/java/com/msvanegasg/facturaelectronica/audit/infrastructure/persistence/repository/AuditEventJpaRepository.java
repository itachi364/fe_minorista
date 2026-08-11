package com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.msvanegasg.facturaelectronica.audit.infrastructure.persistence.entity.AuditEventJpaEntity;

public interface AuditEventJpaRepository extends JpaRepository<AuditEventJpaEntity, UUID> {

    List<AuditEventJpaEntity> findByCompanyIdOrderByOccurredAtDesc(UUID companyId);

    @Query("""
            select distinct event.resourceType
            from AuditEventJpaEntity event
            where event.companyId = :companyId
            order by event.resourceType
            """)
    List<String> findDistinctResourceTypesByCompanyId(UUID companyId);
}

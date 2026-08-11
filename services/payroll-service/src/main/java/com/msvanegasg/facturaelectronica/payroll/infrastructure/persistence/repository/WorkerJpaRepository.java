package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.WorkerJpaEntity;

public interface WorkerJpaRepository extends JpaRepository<WorkerJpaEntity, UUID> {
    List<WorkerJpaEntity> findByCompanyIdOrderByFullName(UUID companyId);
    Optional<WorkerJpaEntity> findByCompanyIdAndId(UUID companyId, UUID id);
}

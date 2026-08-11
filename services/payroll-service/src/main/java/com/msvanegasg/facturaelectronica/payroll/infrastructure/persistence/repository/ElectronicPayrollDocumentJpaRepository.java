package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.ElectronicPayrollDocumentJpaEntity;

public interface ElectronicPayrollDocumentJpaRepository extends JpaRepository<ElectronicPayrollDocumentJpaEntity, UUID> {
    List<ElectronicPayrollDocumentJpaEntity> findByCompanyIdOrderByCreatedAtDesc(UUID companyId);
}

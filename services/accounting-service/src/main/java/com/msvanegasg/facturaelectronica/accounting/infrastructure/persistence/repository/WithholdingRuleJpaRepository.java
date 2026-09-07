package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.WithholdingRuleJpaEntity;

public interface WithholdingRuleJpaRepository extends JpaRepository<WithholdingRuleJpaEntity, UUID> {

    @Query("""
            select rule from WithholdingRuleJpaEntity rule
            where rule.active = true
              and (rule.companyId is null or rule.companyId = :companyId)
              and rule.operationType = :operationType
              and rule.validFrom <= :operationDate
              and (rule.validTo is null or rule.validTo >= :operationDate)
            """)
    List<WithholdingRuleJpaEntity> findActiveRules(UUID companyId, FiscalOperationType operationType,
            LocalDate operationDate);
}

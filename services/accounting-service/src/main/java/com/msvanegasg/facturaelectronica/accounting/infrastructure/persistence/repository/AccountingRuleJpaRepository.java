package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingRuleJpaEntity;

public interface AccountingRuleJpaRepository extends JpaRepository<AccountingRuleJpaEntity, UUID> {

    Optional<AccountingRuleJpaEntity> findByCompanyIdAndEventTypeAndActiveTrue(
            UUID companyId,
            AccountingEventType eventType);

    List<AccountingRuleJpaEntity> findByCompanyIdOrderByEventTypeAscNameAsc(UUID companyId);

    List<AccountingRuleJpaEntity> findByCompanyIdAndEventTypeOrderByActiveDescNameAsc(
            UUID companyId,
            AccountingEventType eventType);

    List<AccountingRuleJpaEntity> findByCompanyIdAndActiveOrderByEventTypeAscNameAsc(UUID companyId, Boolean active);

    List<AccountingRuleJpaEntity> findByCompanyIdAndEventTypeAndActiveOrderByNameAsc(
            UUID companyId,
            AccountingEventType eventType,
            Boolean active);
}
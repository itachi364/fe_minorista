package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingRuleLineJpaEntity;

public interface AccountingRuleLineJpaRepository extends JpaRepository<AccountingRuleLineJpaEntity, UUID> {

    List<AccountingRuleLineJpaEntity> findByRuleIdOrderByLineOrderAsc(UUID ruleId);

    void deleteByRuleId(UUID ruleId);
}

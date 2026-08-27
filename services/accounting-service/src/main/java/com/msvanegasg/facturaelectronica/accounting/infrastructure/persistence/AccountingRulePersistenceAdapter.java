package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingRuleLine;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingRuleJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingRuleLineJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingRuleJpaRepository;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingRuleLineJpaRepository;

@Component
public class AccountingRulePersistenceAdapter implements AccountingRuleRepositoryPort {

    private final AccountingRuleJpaRepository ruleRepository;
    private final AccountingRuleLineJpaRepository lineRepository;

    public AccountingRulePersistenceAdapter(
            AccountingRuleJpaRepository ruleRepository,
            AccountingRuleLineJpaRepository lineRepository) {
        this.ruleRepository = ruleRepository;
        this.lineRepository = lineRepository;
    }

    @Override
    public Optional<AccountingRule> findByCompanyIdAndId(UUID companyId, UUID id) {
        return ruleRepository.findByCompanyIdAndId(companyId, id)
                .map(rule -> toDomain(rule, lineRepository.findByRuleIdOrderByLineOrderAsc(rule.getId())));
    }

    @Override
    public Optional<AccountingRule> findActiveByCompanyIdAndEventType(UUID companyId, AccountingEventType eventType) {
        return ruleRepository.findByCompanyIdAndEventTypeAndActiveTrue(companyId, eventType)
                .map(rule -> toDomain(rule, lineRepository.findByRuleIdOrderByLineOrderAsc(rule.getId())));
    }

    @Override
    public List<AccountingRule> findByCompanyId(UUID companyId, AccountingEventType eventType, Boolean active) {
        List<AccountingRuleJpaEntity> rules;
        if (eventType != null && active != null) {
            rules = ruleRepository.findByCompanyIdAndEventTypeAndActiveOrderByNameAsc(companyId, eventType, active);
        } else if (eventType != null) {
            rules = ruleRepository.findByCompanyIdAndEventTypeOrderByActiveDescNameAsc(companyId, eventType);
        } else if (active != null) {
            rules = ruleRepository.findByCompanyIdAndActiveOrderByEventTypeAscNameAsc(companyId, active);
        } else {
            rules = ruleRepository.findByCompanyIdOrderByEventTypeAscNameAsc(companyId);
        }
        return rules.stream()
                .map(rule -> toDomain(rule, lineRepository.findByRuleIdOrderByLineOrderAsc(rule.getId())))
                .toList();
    }

    @Override
    @Transactional
    public AccountingRule save(AccountingRule rule) {
        AccountingRuleJpaEntity savedRule = ruleRepository.save(toEntity(rule));
        lineRepository.deleteByRuleId(savedRule.getId());
        List<AccountingRuleLineJpaEntity> lines = IntStream.range(0, rule.lines().size())
                .mapToObj(index -> toEntity(rule.lines().get(index), savedRule.getId(), index + 1))
                .map(lineRepository::save)
                .toList();
        return toDomain(savedRule, lines);
    }

    private static AccountingRule toDomain(
            AccountingRuleJpaEntity rule,
            List<AccountingRuleLineJpaEntity> lines) {
        return AccountingRule.restore(
                rule.getId(),
                rule.getCompanyId(),
                rule.getEventType(),
                rule.getSourceType(),
                rule.getName(),
                lines.stream()
                        .map(line -> AccountingRuleLine.create(
                                line.getAccountCode(),
                                line.getSide(),
                                line.getAmountType(),
                                line.getDescription()))
                        .toList(),
                Boolean.TRUE.equals(rule.getActive()));
    }

    private static AccountingRuleJpaEntity toEntity(AccountingRule rule) {
        return AccountingRuleJpaEntity.builder()
                .id(rule.id())
                .companyId(rule.companyId())
                .eventType(rule.eventType())
                .sourceType(rule.sourceType())
                .name(rule.name())
                .active(rule.active())
                .build();
    }

    private static AccountingRuleLineJpaEntity toEntity(
            AccountingRuleLine line,
            UUID ruleId,
            int lineOrder) {
        return AccountingRuleLineJpaEntity.builder()
                .id(UUID.randomUUID())
                .ruleId(ruleId)
                .lineOrder(lineOrder)
                .accountCode(line.accountCode())
                .side(line.side())
                .amountType(line.amountType())
                .description(line.description())
                .build();
    }
}

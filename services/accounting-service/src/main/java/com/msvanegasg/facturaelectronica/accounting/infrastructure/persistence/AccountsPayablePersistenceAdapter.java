package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsPayableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsPayableStatus;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsPayableJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountsPayableJpaRepository;

@Component
public class AccountsPayablePersistenceAdapter implements AccountsPayableRepositoryPort {

    private final AccountsPayableJpaRepository repository;

    public AccountsPayablePersistenceAdapter(AccountsPayableJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AccountsPayable> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(AccountsPayablePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<AccountsPayable> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId) {
        return repository.findByCompanyIdAndSourceTypeAndSourceId(companyId, sourceType, sourceId)
                .map(AccountsPayablePersistenceAdapter::toDomain);
    }

    @Override
    public List<AccountsPayable> find(UUID companyId, AccountsPayableStatus status, UUID supplierId, LocalDate from,
            LocalDate to) {
        LocalDate fromDate = from == null ? LocalDate.of(1900, 1, 1) : from;
        LocalDate toDate = to == null ? LocalDate.of(2999, 12, 31) : to;
        List<AccountsPayableJpaEntity> entities = status == null
                ? repository.findByCompanyIdAndDueDateBetweenOrderByDueDateAsc(companyId, fromDate, toDate)
                : repository.findByCompanyIdAndStatusAndDueDateBetweenOrderByDueDateAsc(companyId, status, fromDate,
                        toDate);
        return entities.stream()
                .filter(entity -> supplierId == null || supplierId.equals(entity.getSupplierId()))
                .map(AccountsPayablePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public AccountsPayable save(AccountsPayable payable) {
        return toDomain(repository.save(toEntity(payable)));
    }

    private static AccountsPayable toDomain(AccountsPayableJpaEntity entity) {
        return new AccountsPayable(entity.getId(), entity.getCompanyId(), entity.getSupplierId(),
                entity.getSourceType(), entity.getSourceId(), entity.getIssueDate(), entity.getDueDate(),
                entity.getTotalAmount(), entity.getPaidAmount(), entity.getStatus(), entity.getCreatedAt());
    }

    private static AccountsPayableJpaEntity toEntity(AccountsPayable payable) {
        AccountsPayableJpaEntity entity = new AccountsPayableJpaEntity();
        entity.setId(payable.id());
        entity.setCompanyId(payable.companyId());
        entity.setSupplierId(payable.supplierId());
        entity.setSourceType(payable.sourceType());
        entity.setSourceId(payable.sourceId());
        entity.setIssueDate(payable.issueDate());
        entity.setDueDate(payable.dueDate());
        entity.setTotalAmount(payable.totalAmount());
        entity.setPaidAmount(payable.paidAmount());
        entity.setStatus(payable.status());
        entity.setCreatedAt(payable.createdAt());
        return entity;
    }
}

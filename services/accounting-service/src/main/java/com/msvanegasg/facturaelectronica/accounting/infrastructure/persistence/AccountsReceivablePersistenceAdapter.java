package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountsReceivableRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivable;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountsReceivableStatus;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountsReceivableJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountsReceivableJpaRepository;

@Component
public class AccountsReceivablePersistenceAdapter implements AccountsReceivableRepositoryPort {

    private final AccountsReceivableJpaRepository repository;

    public AccountsReceivablePersistenceAdapter(AccountsReceivableJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AccountsReceivable> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(AccountsReceivablePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<AccountsReceivable> findByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId) {
        return repository.findByCompanyIdAndSourceTypeAndSourceId(companyId, sourceType, sourceId)
                .map(AccountsReceivablePersistenceAdapter::toDomain);
    }

    @Override
    public Optional<AccountsReceivable> findByCompanyIdAndIdempotencyKey(UUID companyId, String idempotencyKey) {
        return repository.findByCompanyIdAndIdempotencyKey(companyId, idempotencyKey)
                .map(AccountsReceivablePersistenceAdapter::toDomain);
    }

    @Override
    public List<AccountsReceivable> find(UUID companyId, AccountsReceivableStatus status, UUID customerId,
            LocalDate from, LocalDate to) {
        LocalDate fromDate = from == null ? LocalDate.of(1900, 1, 1) : from;
        LocalDate toDate = to == null ? LocalDate.of(2999, 12, 31) : to;
        List<AccountsReceivableJpaEntity> entities = status == null
                ? repository.findByCompanyIdAndDueDateBetweenOrderByDueDateAsc(companyId, fromDate, toDate)
                : repository.findByCompanyIdAndStatusAndDueDateBetweenOrderByDueDateAsc(companyId, status, fromDate,
                        toDate);
        return entities.stream()
                .filter(entity -> customerId == null || customerId.equals(entity.getCustomerId()))
                .map(AccountsReceivablePersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public AccountsReceivable save(AccountsReceivable receivable) {
        return toDomain(repository.save(toEntity(receivable)));
    }

    private static AccountsReceivable toDomain(AccountsReceivableJpaEntity entity) {
        return new AccountsReceivable(entity.getId(), entity.getCompanyId(), entity.getCustomerId(),
                entity.getSourceType(), entity.getSourceId(), entity.getIssueDate(), entity.getDueDate(),
                entity.getTotalAmount(), entity.getPaidAmount(), entity.getStatus(), entity.getIdempotencyKey(),
                entity.getCreatedAt());
    }

    private static AccountsReceivableJpaEntity toEntity(AccountsReceivable receivable) {
        AccountsReceivableJpaEntity entity = new AccountsReceivableJpaEntity();
        entity.setId(receivable.id());
        entity.setCompanyId(receivable.companyId());
        entity.setCustomerId(receivable.customerId());
        entity.setSourceType(receivable.sourceType());
        entity.setSourceId(receivable.sourceId());
        entity.setIssueDate(receivable.issueDate());
        entity.setDueDate(receivable.dueDate());
        entity.setTotalAmount(receivable.totalAmount());
        entity.setPaidAmount(receivable.paidAmount());
        entity.setStatus(receivable.status());
        entity.setIdempotencyKey(receivable.idempotencyKey());
        entity.setCreatedAt(receivable.createdAt());
        return entity;
    }
}
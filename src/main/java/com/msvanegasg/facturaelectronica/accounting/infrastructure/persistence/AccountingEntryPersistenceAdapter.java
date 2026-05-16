package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryLine;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingEntryJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.entity.AccountingEntryLineJpaEntity;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingEntryJpaRepository;
import com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence.repository.AccountingEntryLineJpaRepository;

@Component
public class AccountingEntryPersistenceAdapter implements AccountingEntryRepositoryPort {

    private final AccountingEntryJpaRepository entryRepository;
    private final AccountingEntryLineJpaRepository lineRepository;

    public AccountingEntryPersistenceAdapter(
            AccountingEntryJpaRepository entryRepository,
            AccountingEntryLineJpaRepository lineRepository) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
    }

    @Override
    public boolean existsByCompanyIdAndSource(UUID companyId, AccountingSourceType sourceType, UUID sourceId) {
        return entryRepository.existsByCompanyIdAndSourceTypeAndSourceId(companyId, sourceType, sourceId);
    }

    @Override
    @Transactional
    public AccountingEntry save(AccountingEntry entry) {
        AccountingEntryJpaEntity savedEntry = entryRepository.save(toEntity(entry));
        lineRepository.deleteByEntryId(savedEntry.getId());
        List<AccountingEntryLineJpaEntity> lines = IntStream.range(0, entry.lines().size())
                .mapToObj(index -> toEntity(entry.lines().get(index), savedEntry.getId(), index + 1))
                .map(lineRepository::save)
                .toList();
        return toDomain(savedEntry, lines);
    }

    @Override
    public List<AccountingEntry> findPostedByCompanyIdAndEntryDateBetween(
            UUID companyId,
            LocalDate fromDate,
            LocalDate toDate) {
        return entryRepository.findByCompanyIdAndStatusAndEntryDateBetween(
                companyId,
                AccountingEntryStatus.POSTED,
                fromDate,
                toDate).stream()
                .map(entry -> toDomain(entry, lineRepository.findByEntryIdOrderByLineOrderAsc(entry.getId())))
                .toList();
    }

    private static AccountingEntry toDomain(
            AccountingEntryJpaEntity entry,
            List<AccountingEntryLineJpaEntity> lines) {
        return AccountingEntry.post(
                entry.getId(),
                entry.getCompanyId(),
                entry.getEntryDate(),
                entry.getDescription(),
                entry.getSourceType(),
                entry.getSourceId(),
                lines.stream().map(AccountingEntryPersistenceAdapter::toDomainLine).toList());
    }

    private static AccountingEntryLine toDomainLine(AccountingEntryLineJpaEntity line) {
        return AccountingEntryLine.create(
                line.getId(),
                line.getAccountId(),
                line.getAccountCode(),
                line.getAccountName(),
                line.getThirdpartyId(),
                line.getDebitAmount(),
                line.getCreditAmount(),
                line.getDescription());
    }

    private static AccountingEntryJpaEntity toEntity(AccountingEntry entry) {
        return AccountingEntryJpaEntity.builder()
                .id(entry.id())
                .companyId(entry.companyId())
                .entryDate(entry.entryDate())
                .description(entry.description())
                .sourceType(entry.sourceType())
                .sourceId(entry.sourceId())
                .status(entry.status())
                .debitTotal(entry.debitTotal())
                .creditTotal(entry.creditTotal())
                .build();
    }

    private static AccountingEntryLineJpaEntity toEntity(
            AccountingEntryLine line,
            UUID entryId,
            int lineOrder) {
        return AccountingEntryLineJpaEntity.builder()
                .id(line.id())
                .entryId(entryId)
                .lineOrder(lineOrder)
                .accountId(line.accountId())
                .accountCode(line.accountCode())
                .accountName(line.accountName())
                .thirdpartyId(line.thirdpartyId())
                .debitAmount(line.debitAmount())
                .creditAmount(line.creditAmount())
                .description(line.description())
                .build();
    }
}

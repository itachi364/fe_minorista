package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookEntryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookLineResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.JournalBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerAccountSummaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookQuery;
import com.msvanegasg.facturaelectronica.accounting.application.dto.LedgerBookResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.QueryAccountingBooksUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.AccountingEntryRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.Account;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountNature;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntry;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEntryLine;

public class QueryAccountingBooksService implements QueryAccountingBooksUseCase {

    private final AccountingEntryRepositoryPort entryRepository;
    private final AccountRepositoryPort accountRepository;

    public QueryAccountingBooksService(
            AccountingEntryRepositoryPort entryRepository,
            AccountRepositoryPort accountRepository) {
        this.entryRepository = Objects.requireNonNull(entryRepository);
        this.accountRepository = Objects.requireNonNull(accountRepository);
    }

    @Override
    public JournalBookResult journalBook(JournalBookQuery query) {
        validate(query.companyId(), query.fromDate(), query.toDate());
        List<AccountingEntry> entries = findPostedEntries(query.companyId(), query.fromDate(), query.toDate());
        return new JournalBookResult(
                query.companyId(),
                query.fromDate(),
                query.toDate(),
                totalDebit(entries),
                totalCredit(entries),
                entries.stream().map(this::toJournalEntry).toList());
    }

    @Override
    public LedgerBookResult ledgerBook(LedgerBookQuery query) {
        validate(query.companyId(), query.fromDate(), query.toDate());
        List<AccountingEntry> entries = findPostedEntries(query.companyId(), query.fromDate(), query.toDate());
        Map<String, LedgerAccumulator> accumulators = new LinkedHashMap<>();
        for (AccountingEntry entry : entries) {
            for (AccountingEntryLine line : entry.lines()) {
                LedgerAccumulator accumulator = accumulators.computeIfAbsent(
                        line.accountCode(),
                        accountCode -> accumulatorFor(query.companyId(), line));
                accumulator.add(line);
            }
        }

        return new LedgerBookResult(
                query.companyId(),
                query.fromDate(),
                query.toDate(),
                totalDebit(entries),
                totalCredit(entries),
                accumulators.values().stream()
                        .sorted(Comparator.comparing(LedgerAccumulator::accountCode))
                        .map(LedgerAccumulator::toResult)
                        .toList());
    }

    private List<AccountingEntry> findPostedEntries(UUID companyId, LocalDate fromDate, LocalDate toDate) {
        return entryRepository.findPostedByCompanyIdAndEntryDateBetween(companyId, fromDate, toDate).stream()
                .sorted(Comparator.comparing(AccountingEntry::entryDate)
                        .thenComparing(AccountingEntry::description)
                        .thenComparing(entry -> entry.id().toString()))
                .toList();
    }

    private JournalBookEntryResult toJournalEntry(AccountingEntry entry) {
        return new JournalBookEntryResult(
                entry.id(),
                entry.entryDate(),
                entry.description(),
                entry.sourceType(),
                entry.sourceId(),
                entry.debitTotal(),
                entry.creditTotal(),
                entry.lines().stream().map(this::toJournalLine).toList());
    }

    private JournalBookLineResult toJournalLine(AccountingEntryLine line) {
        return new JournalBookLineResult(
                line.id(),
                line.accountId(),
                line.accountCode(),
                line.accountName(),
                line.thirdpartyId(),
                line.debitAmount(),
                line.creditAmount(),
                line.description());
    }

    private LedgerAccumulator accumulatorFor(UUID companyId, AccountingEntryLine line) {
        Account account = accountRepository.findByCompanyIdAndCode(companyId, line.accountCode())
                .orElseThrow(() -> new IllegalStateException("account was not found: " + line.accountCode()));
        return new LedgerAccumulator(account.id(), account.code(), account.name(), account.nature());
    }

    private static BigDecimal totalDebit(List<AccountingEntry> entries) {
        return entries.stream()
                .map(AccountingEntry::debitTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal totalCredit(List<AccountingEntry> entries) {
        return entries.stream()
                .map(AccountingEntry::creditTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static void validate(UUID companyId, LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(fromDate, "fromDate is required");
        Objects.requireNonNull(toDate, "toDate is required");
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
    }

    private static final class LedgerAccumulator {

        private final UUID accountId;
        private final String accountCode;
        private final String accountName;
        private final AccountNature nature;
        private final List<AccountingEntryLine> lines = new ArrayList<>();

        private LedgerAccumulator(UUID accountId, String accountCode, String accountName, AccountNature nature) {
            this.accountId = accountId;
            this.accountCode = accountCode;
            this.accountName = accountName;
            this.nature = nature;
        }

        void add(AccountingEntryLine line) {
            lines.add(line);
        }

        String accountCode() {
            return accountCode;
        }

        LedgerAccountSummaryResult toResult() {
            BigDecimal debitTotal = lines.stream()
                    .map(AccountingEntryLine::debitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal creditTotal = lines.stream()
                    .map(AccountingEntryLine::creditAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal balance = switch (nature) {
                case DEBIT -> debitTotal.subtract(creditTotal);
                case CREDIT -> creditTotal.subtract(debitTotal);
            };
            return new LedgerAccountSummaryResult(
                    accountId,
                    accountCode,
                    accountName,
                    nature,
                    debitTotal,
                    creditTotal,
                    balance);
        }
    }
}

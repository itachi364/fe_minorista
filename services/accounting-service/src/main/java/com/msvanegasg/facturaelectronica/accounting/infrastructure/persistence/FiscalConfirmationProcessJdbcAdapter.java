package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalConfirmationProcessRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;

@Repository
public class FiscalConfirmationProcessJdbcAdapter implements FiscalConfirmationProcessRepositoryPort {

    private final JdbcTemplate jdbcTemplate;

    public FiscalConfirmationProcessJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void markProcessing(UUID processId, UUID companyId, AccountingSourceType sourceType, UUID sourceId,
            Instant updatedAt) {
        jdbcTemplate.update("INSERT INTO fiscal_confirmation_process "
                + "(id, company_id, source_type, source_id, status, attempt_count, updated_at) "
                + "VALUES (?, ?, ?, ?, 'PROCESSING', 1, ?) ON CONFLICT (company_id, source_type, source_id) "
                + "DO UPDATE SET status = 'PROCESSING', last_error = NULL, "
                + "attempt_count = fiscal_confirmation_process.attempt_count + 1, updated_at = EXCLUDED.updated_at",
                processId, companyId, sourceType.name(), sourceId, updatedAt);
    }

    @Override
    public void markCompleted(UUID companyId, AccountingSourceType sourceType, UUID sourceId, UUID calculationId,
            UUID accountingEntryId, UUID payableId, Instant updatedAt) {
        jdbcTemplate.update("UPDATE fiscal_confirmation_process SET status = 'COMPLETED', calculation_id = ?, "
                + "accounting_entry_id = ?, payable_id = ?, last_error = NULL, updated_at = ? "
                + "WHERE company_id = ? AND source_type = ? AND source_id = ?", calculationId,
                accountingEntryId, payableId, updatedAt, companyId, sourceType.name(), sourceId);
    }

    @Override
    public void markFailed(UUID processId, UUID companyId, AccountingSourceType sourceType, UUID sourceId,
            String lastError, Instant updatedAt) {
        jdbcTemplate.update("INSERT INTO fiscal_confirmation_process "
                + "(id, company_id, source_type, source_id, status, last_error, attempt_count, updated_at) "
                + "VALUES (?, ?, ?, ?, 'FAILED', ?, 1, ?) ON CONFLICT (company_id, source_type, source_id) "
                + "DO UPDATE SET status = 'FAILED', last_error = EXCLUDED.last_error, "
                + "updated_at = EXCLUDED.updated_at", processId, companyId, sourceType.name(), sourceId,
                lastError, updatedAt);
    }
}

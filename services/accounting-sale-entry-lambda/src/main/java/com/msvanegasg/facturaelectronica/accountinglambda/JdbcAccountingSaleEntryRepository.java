package com.msvanegasg.facturaelectronica.accountinglambda;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class JdbcAccountingSaleEntryRepository implements AccountingSaleEntryRepositoryPort {

    static final String CONSUMER_NAME = "accounting-sale-entry-lambda";

    private final DatabaseSettings settings;
    private final ObjectMapper objectMapper;

    public JdbcAccountingSaleEntryRepository(DatabaseSettings settings) {
        this(settings, new ObjectMapper());
    }

    JdbcAccountingSaleEntryRepository(DatabaseSettings settings, ObjectMapper objectMapper) {
        this.settings = Objects.requireNonNull(settings, "settings is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    @Override
    public boolean applyIfNew(DomainEventEnvelope envelope, AccountingSaleEntryRequest request) {
        Objects.requireNonNull(envelope, "envelope is required");
        Objects.requireNonNull(request, "request is required");
        try (Connection connection = DriverManager.getConnection(settings.url(), settings.username(),
                settings.password())) {
            connection.setAutoCommit(false);
            try {
                int inboxRows = insertInbox(connection, envelope);
                if (inboxRows == 0) {
                    connection.commit();
                    return false;
                }
                if (!entryExists(connection, request.companyId(), request.saleId())) {
                    UUID entryId = UUID.randomUUID();
                    List<AccountingLine> lines = buildEntryLines(connection, request);
                    BigDecimal debitTotal = lines.stream().map(AccountingLine::debitAmount).reduce(BigDecimal.ZERO,
                            BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal creditTotal = lines.stream().map(AccountingLine::creditAmount).reduce(BigDecimal.ZERO,
                            BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
                    if (debitTotal.compareTo(creditTotal) != 0) {
                        throw new IllegalStateException("accounting entry must be balanced");
                    }
                    insertEntry(connection, request, entryId, debitTotal, creditTotal);
                    insertLines(connection, entryId, lines);
                    insertAccountingEntryPostedOutbox(connection, envelope, request, entryId, debitTotal, creditTotal);
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                rollback(connection);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new AccountingSaleEntryPersistenceException("Cannot apply accounting sale entry", exception);
        }
    }

    private int insertInbox(Connection connection, DomainEventEnvelope envelope) throws SQLException {
        String sql = """
                INSERT INTO accounting_inbox_event (id, event_id, event_type, company_id, consumer, processed_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id, consumer) DO NOTHING
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, envelope.eventId());
            statement.setString(3, envelope.eventType());
            statement.setObject(4, envelope.companyId());
            statement.setString(5, CONSUMER_NAME);
            statement.setTimestamp(6, Timestamp.from(Instant.now()));
            return statement.executeUpdate();
        }
    }

    private boolean entryExists(Connection connection, UUID companyId, UUID saleId) throws SQLException {
        String sql = """
                SELECT 1
                FROM accounting_entry
                WHERE company_id = ? AND source_type = 'SALE' AND source_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companyId);
            statement.setObject(2, saleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private List<AccountingLine> buildEntryLines(Connection connection, AccountingSaleEntryRequest request)
            throws SQLException {
        UUID ruleId = findSaleRuleId(connection, request.companyId());
        List<AccountingLine> lines = new ArrayList<>();
        String sql = """
                SELECT account_code, side, amount_type, description
                FROM accounting_rule_line
                WHERE rule_id = ?
                ORDER BY line_order ASC
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, ruleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    BigDecimal amount = amountOf(request, resultSet.getString("amount_type"));
                    if (amount.signum() == 0) {
                        continue;
                    }
                    Account account = findActiveAccount(connection, request.companyId(), resultSet.getString("account_code"));
                    String side = resultSet.getString("side");
                    BigDecimal debit = "DEBIT".equals(side) ? amount : BigDecimal.ZERO.setScale(2);
                    BigDecimal credit = "CREDIT".equals(side) ? amount : BigDecimal.ZERO.setScale(2);
                    lines.add(new AccountingLine(UUID.randomUUID(), account.id(), account.code(), account.name(),
                            request.thirdpartyId(), debit, credit, resultSet.getString("description")));
                }
            }
        }
        if (lines.size() < 2) {
            throw new IllegalArgumentException("accounting entry requires at least two lines");
        }
        return lines;
    }

    private UUID findSaleRuleId(Connection connection, UUID companyId) throws SQLException {
        String sql = """
                SELECT id, source_type
                FROM accounting_rule
                WHERE company_id = ? AND event_type = 'SALE_CONFIRMED' AND active = true
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("accounting rule was not found");
                }
                if (!"SALE".equals(resultSet.getString("source_type"))) {
                    throw new IllegalStateException("accounting rule source type does not match command source type");
                }
                return resultSet.getObject("id", UUID.class);
            }
        }
    }

    private Account findActiveAccount(Connection connection, UUID companyId, String accountCode) throws SQLException {
        String sql = """
                SELECT id, code, name, active
                FROM accounting_account
                WHERE company_id = ? AND code = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companyId);
            statement.setString(2, accountCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("account was not found: " + accountCode);
                }
                if (!resultSet.getBoolean("active")) {
                    throw new IllegalStateException("account is inactive: " + accountCode);
                }
                return new Account(resultSet.getObject("id", UUID.class), resultSet.getString("code"),
                        resultSet.getString("name"));
            }
        }
    }

    private static BigDecimal amountOf(AccountingSaleEntryRequest request, String amountType) {
        BigDecimal amount = switch (amountType) {
            case "SUBTOTAL" -> request.subtotal();
            case "TAX_TOTAL" -> request.taxTotal();
            case "TOTAL" -> request.total();
            default -> throw new IllegalArgumentException("unsupported accounting amount type: " + amountType);
        };
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private void insertEntry(Connection connection, AccountingSaleEntryRequest request, UUID entryId,
            BigDecimal debitTotal, BigDecimal creditTotal) throws SQLException {
        String sql = """
                INSERT INTO accounting_entry (id, company_id, entry_date, description, source_type, source_id, status,
                    debit_total, credit_total)
                VALUES (?, ?, ?, ?, 'SALE', ?, 'POSTED', ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, entryId);
            statement.setObject(2, request.companyId());
            statement.setObject(3, request.entryDate());
            statement.setString(4, request.description());
            statement.setObject(5, request.saleId());
            statement.setBigDecimal(6, debitTotal);
            statement.setBigDecimal(7, creditTotal);
            statement.executeUpdate();
        }
    }

    private void insertLines(Connection connection, UUID entryId, List<AccountingLine> lines) throws SQLException {
        String sql = """
                INSERT INTO accounting_entry_line (id, entry_id, line_order, account_id, account_code, account_name,
                    thirdparty_id, debit_amount, credit_amount, description)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < lines.size(); index++) {
                AccountingLine line = lines.get(index);
                statement.setObject(1, line.id());
                statement.setObject(2, entryId);
                statement.setInt(3, index + 1);
                statement.setObject(4, line.accountId());
                statement.setString(5, line.accountCode());
                statement.setString(6, line.accountName());
                if (line.thirdpartyId() == null) {
                    statement.setNull(7, Types.OTHER);
                } else {
                    statement.setObject(7, line.thirdpartyId());
                }
                statement.setBigDecimal(8, line.debitAmount());
                statement.setBigDecimal(9, line.creditAmount());
                statement.setString(10, line.description());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertAccountingEntryPostedOutbox(Connection connection, DomainEventEnvelope envelope,
            AccountingSaleEntryRequest request, UUID entryId, BigDecimal debitTotal, BigDecimal creditTotal)
            throws SQLException {
        String sql = """
                INSERT INTO accounting_outbox_event (event_id, event_type, event_version, occurred_at, company_id,
                    aggregate_type, aggregate_id, producer, correlation_id, idempotency_key, payload_json, status,
                    publish_attempts, created_at)
                VALUES (?, ?, 1, ?, ?, 'AccountingEntry', ?, 'accounting-service', ?, ?, ?, 'PENDING', 0, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            Instant now = Instant.now();
            statement.setObject(1, UUID.randomUUID());
            statement.setString(2, EventTypes.ACCOUNTING_ENTRY_POSTED);
            statement.setTimestamp(3, Timestamp.from(now));
            statement.setObject(4, request.companyId());
            statement.setObject(5, entryId);
            statement.setString(6, envelope.correlationId());
            statement.setString(7, "SALE:" + request.saleId() + ":accounting-entry-posted");
            statement.setString(8, outboxPayload(request, entryId, debitTotal, creditTotal));
            statement.setTimestamp(9, Timestamp.from(now));
            statement.executeUpdate();
        }
    }

    private String outboxPayload(AccountingSaleEntryRequest request, UUID entryId, BigDecimal debitTotal,
            BigDecimal creditTotal) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("entryId", entryId.toString());
        payload.put("entryDate", request.entryDate().toString());
        payload.put("description", request.description());
        payload.put("sourceType", "SALE");
        payload.put("sourceId", request.saleId().toString());
        payload.put("status", "POSTED");
        payload.put("debitTotal", debitTotal);
        payload.put("creditTotal", creditTotal);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize accounting entry outbox payload", exception);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original persistence error.
        }
    }

    private record Account(UUID id, String code, String name) {
    }

    private record AccountingLine(UUID id, UUID accountId, String accountCode, String accountName, UUID thirdpartyId,
            BigDecimal debitAmount, BigDecimal creditAmount, String description) {
    }

    public static class AccountingSaleEntryPersistenceException extends RuntimeException {
        public AccountingSaleEntryPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
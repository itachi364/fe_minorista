package com.msvanegasg.facturaelectronica.providerretry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class JdbcBillingDocumentRetryRepository implements BillingDocumentRetryRepositoryPort {

    private final DatabaseSettings settings;
    private final ObjectMapper objectMapper;

    public JdbcBillingDocumentRetryRepository(DatabaseSettings settings, ObjectMapper objectMapper) {
        this.settings = Objects.requireNonNull(settings, "settings is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    @Override
    public Optional<BillingDocumentSnapshot> findDocument(UUID companyId, UUID documentId) {
        String sql = """
                SELECT s.id AS sale_id, s.company_id, s.customer_id, s.sale_channel, s.status AS sale_status,
                       d.id AS document_id, d.document_type, d.status AS document_status, d.provider_status,
                       d.prefix, d.document_number, d.cufe_cude, d.qr_content, d.subtotal, d.tax_total, d.total,
                       d.idempotency_key, d.issued_at
                FROM billing.electronic_document d
                JOIN billing.sale s ON s.id = d.sale_id AND s.company_id = d.company_id
                WHERE d.company_id = ? AND d.id = ?
                """;
        try (Connection connection = DriverManager.getConnection(settings.url(), settings.username(), settings.password());
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companyId);
            statement.setObject(2, documentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                BillingDocumentSnapshot header = toSnapshot(resultSet, lines(connection, resultSet.getObject("sale_id", UUID.class)));
                return Optional.of(header);
            }
        } catch (SQLException exception) {
            throw new ProviderRetryPersistenceException("Cannot load failed provider submission", exception);
        }
    }

    @Override
    public void markAcceptedAndPublish(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome,
            DomainEventEnvelope saleConfirmed, DomainEventEnvelope documentValidated, Instant retriedAt) {
        withTransaction(connection -> {
            int updated = updateDocument(connection, snapshot, ElectronicDocumentStatus.VALIDATED, ProviderStatus.ACCEPTED,
                    outcome, retriedAt);
            if (updated > 0) {
                insertOutbox(connection, saleConfirmed);
                insertOutbox(connection, documentValidated);
            }
        });
    }

    @Override
    public void markRejected(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome, Instant retriedAt) {
        withTransaction(connection -> updateDocument(connection, snapshot, ElectronicDocumentStatus.REJECTED,
                ProviderStatus.REJECTED, outcome, retriedAt));
    }

    @Override
    public void markFailed(BillingDocumentSnapshot snapshot, ProviderSubmissionOutcome outcome, Instant retriedAt) {
        withTransaction(connection -> updateDocument(connection, snapshot, ElectronicDocumentStatus.FAILED,
                ProviderStatus.FAILED, outcome, retriedAt));
    }

    private BillingDocumentSnapshot toSnapshot(ResultSet resultSet, List<SaleLineSnapshot> lines) throws SQLException {
        return new BillingDocumentSnapshot(
                resultSet.getObject("company_id", UUID.class),
                resultSet.getObject("sale_id", UUID.class),
                resultSet.getObject("customer_id", UUID.class),
                resultSet.getString("sale_channel"),
                resultSet.getString("sale_status"),
                resultSet.getObject("document_id", UUID.class),
                resultSet.getString("document_type"),
                ElectronicDocumentStatus.valueOf(resultSet.getString("document_status")),
                ProviderStatus.valueOf(resultSet.getString("provider_status")),
                resultSet.getString("prefix"),
                resultSet.getLong("document_number"),
                resultSet.getString("cufe_cude"),
                resultSet.getString("qr_content"),
                resultSet.getBigDecimal("subtotal"),
                resultSet.getBigDecimal("tax_total"),
                resultSet.getBigDecimal("total"),
                resultSet.getString("idempotency_key"),
                resultSet.getTimestamp("issued_at").toInstant(),
                lines);
    }

    private List<SaleLineSnapshot> lines(Connection connection, UUID saleId) throws SQLException {
        String sql = """
                SELECT id, product_id, product_sku, product_name, item_type, stock_tracked, quantity, unit_cost,
                       unit_price, discount_amount, tax_code, tax_rate, subtotal, tax_amount, total
                FROM billing.sale_line
                WHERE sale_id = ?
                ORDER BY id
                """;
        List<SaleLineSnapshot> lines = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, saleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lines.add(new SaleLineSnapshot(
                            resultSet.getObject("id", UUID.class),
                            resultSet.getObject("product_id", UUID.class),
                            resultSet.getString("product_sku"),
                            resultSet.getString("product_name"),
                            resultSet.getString("item_type"),
                            resultSet.getBoolean("stock_tracked"),
                            resultSet.getBigDecimal("quantity"),
                            resultSet.getBigDecimal("unit_cost"),
                            resultSet.getBigDecimal("unit_price"),
                            resultSet.getBigDecimal("discount_amount"),
                            resultSet.getString("tax_code"),
                            resultSet.getBigDecimal("tax_rate"),
                            resultSet.getBigDecimal("subtotal"),
                            resultSet.getBigDecimal("tax_amount"),
                            resultSet.getBigDecimal("total")));
                }
            }
        }
        return lines;
    }

    private int updateDocument(Connection connection, BillingDocumentSnapshot snapshot, ElectronicDocumentStatus status,
            ProviderStatus providerStatus, ProviderSubmissionOutcome outcome, Instant retriedAt) throws SQLException {
        String sql = """
                UPDATE billing.electronic_document
                SET status = ?, provider_status = ?, provider_tracking_id = ?, provider_error_code = ?,
                    provider_error_message = ?, cufe_cude = COALESCE(NULLIF(?, ''), cufe_cude),
                    qr_content = COALESCE(NULLIF(?, ''), qr_content),
                    provider_retry_attempts = provider_retry_attempts + 1,
                    provider_last_retry_at = ?
                WHERE company_id = ? AND id = ? AND status <> 'VALIDATED'
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setString(2, providerStatus.name());
            statement.setString(3, nullToFallback(outcome.trackingId(), snapshot.documentId().toString()));
            setNullableText(statement, 4, outcome.errorCode());
            setNullableText(statement, 5, outcome.errorMessage());
            statement.setString(6, blankToNull(outcome.cufeCude()));
            statement.setString(7, blankToNull(outcome.qrContent()));
            statement.setTimestamp(8, Timestamp.from(retriedAt));
            statement.setObject(9, snapshot.companyId());
            statement.setObject(10, snapshot.documentId());
            return statement.executeUpdate();
        }
    }

    private void insertOutbox(Connection connection, DomainEventEnvelope event) throws SQLException {
        String sql = """
                INSERT INTO billing.outbox_event (event_id, event_type, event_version, occurred_at, company_id,
                    aggregate_type, aggregate_id, producer, correlation_id, idempotency_key, payload_json, status,
                    publish_attempts, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING', 0, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, event.eventId());
            statement.setString(2, event.eventType());
            statement.setInt(3, event.eventVersion());
            statement.setTimestamp(4, Timestamp.from(event.occurredAt()));
            statement.setObject(5, event.companyId());
            statement.setString(6, event.aggregateType());
            statement.setObject(7, event.aggregateId());
            statement.setString(8, event.producer());
            setNullableText(statement, 9, event.correlationId());
            statement.setString(10, event.idempotencyKey());
            statement.setString(11, payloadJson(event));
            statement.setTimestamp(12, Timestamp.from(event.occurredAt()));
            statement.executeUpdate();
        }
    }

    private String payloadJson(DomainEventEnvelope event) {
        try {
            return objectMapper.writeValueAsString(event.payload());
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("event payload could not be serialized", exception);
        }
    }

    private void withTransaction(SqlWork work) {
        try (Connection connection = DriverManager.getConnection(settings.url(), settings.username(), settings.password())) {
            connection.setAutoCommit(false);
            try {
                work.execute(connection);
                connection.commit();
            } catch (Exception exception) {
                rollback(connection);
                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new IllegalStateException(exception);
            }
        } catch (SQLException exception) {
            throw new ProviderRetryPersistenceException("Cannot update provider retry outcome", exception);
        }
    }

    private static void setNullableText(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private static String nullToFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original persistence error.
        }
    }

    @FunctionalInterface
    private interface SqlWork {
        void execute(Connection connection) throws Exception;
    }

    public static class ProviderRetryPersistenceException extends RuntimeException {
        public ProviderRetryPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
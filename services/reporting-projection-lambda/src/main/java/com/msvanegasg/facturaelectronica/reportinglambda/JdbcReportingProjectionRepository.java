package com.msvanegasg.facturaelectronica.reportinglambda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Objects;

public class JdbcReportingProjectionRepository implements ReportingProjectionRepositoryPort {

    private static final String CONSUMER_NAME = "reporting-projection-lambda";

    private final DatabaseSettings settings;

    public JdbcReportingProjectionRepository(DatabaseSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings is required");
    }

    @Override
    public boolean projectIfNew(ReportingProjectionRequest request) {
        try (Connection connection = DriverManager.getConnection(settings.url(), settings.username(), settings.password())) {
            connection.setAutoCommit(false);
            try {
                ensureSchema(connection);
                boolean inserted = insertInbox(connection, request);
                if (inserted) {
                    insertProjection(connection, request);
                }
                connection.commit();
                return inserted;
            } catch (SQLException exception) {
                rollback(connection);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new ReportingProjectionPersistenceException("Cannot materialize reporting projection", exception);
        }
    }

    private void ensureSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE SCHEMA IF NOT EXISTS reporting
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS reporting.reporting_inbox_event (
                        event_id uuid NOT NULL,
                        consumer_name varchar(100) NOT NULL,
                        event_type varchar(120) NOT NULL,
                        idempotency_key varchar(255) NOT NULL,
                        received_at timestamptz NOT NULL DEFAULT now(),
                        processed_at timestamptz NOT NULL DEFAULT now(),
                        PRIMARY KEY (event_id, consumer_name)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS reporting.reporting_event_projection (
                        event_id uuid PRIMARY KEY,
                        event_type varchar(120) NOT NULL,
                        event_version integer NOT NULL,
                        occurred_at timestamptz NOT NULL,
                        company_id uuid NOT NULL,
                        aggregate_type varchar(120) NOT NULL,
                        aggregate_id uuid NOT NULL,
                        producer varchar(120) NOT NULL,
                        correlation_id varchar(120),
                        idempotency_key varchar(255) NOT NULL,
                        sale_id uuid,
                        document_id uuid,
                        movement_id uuid,
                        accounting_entry_id uuid,
                        product_id uuid,
                        status varchar(80),
                        amount numeric(19, 2) NOT NULL,
                        payload_json jsonb NOT NULL,
                        projected_at timestamptz NOT NULL DEFAULT now()
                    )
                    """);
            statement.execute("""
                    CREATE INDEX IF NOT EXISTS idx_reporting_event_projection_company_period
                    ON reporting.reporting_event_projection (company_id, occurred_at, event_type)
                    """);
        }
    }

    private boolean insertInbox(Connection connection, ReportingProjectionRequest request) throws SQLException {
        String sql = """
                INSERT INTO reporting.reporting_inbox_event (event_id, consumer_name, event_type, idempotency_key)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (event_id, consumer_name) DO NOTHING
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, request.eventId());
            statement.setString(2, CONSUMER_NAME);
            statement.setString(3, request.eventType());
            statement.setString(4, request.idempotencyKey());
            return statement.executeUpdate() > 0;
        }
    }

    private void insertProjection(Connection connection, ReportingProjectionRequest request) throws SQLException {
        String sql = """
                INSERT INTO reporting.reporting_event_projection (
                    event_id, event_type, event_version, occurred_at, company_id, aggregate_type, aggregate_id,
                    producer, correlation_id, idempotency_key, sale_id, document_id, movement_id,
                    accounting_entry_id, product_id, status, amount, payload_json)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                ON CONFLICT (event_id) DO NOTHING
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, request.eventId());
            statement.setString(2, request.eventType());
            statement.setInt(3, request.eventVersion());
            statement.setTimestamp(4, Timestamp.from(request.occurredAt()));
            statement.setObject(5, request.companyId());
            statement.setString(6, request.aggregateType());
            statement.setObject(7, request.aggregateId());
            statement.setString(8, request.producer());
            setNullableText(statement, 9, request.correlationId());
            statement.setString(10, request.idempotencyKey());
            statement.setObject(11, request.saleId());
            statement.setObject(12, request.documentId());
            statement.setObject(13, request.movementId());
            statement.setObject(14, request.accountingEntryId());
            statement.setObject(15, request.productId());
            setNullableText(statement, 16, request.status());
            statement.setBigDecimal(17, request.amount());
            statement.setString(18, request.payloadJson());
            statement.executeUpdate();
        }
    }

    private static void setNullableText(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original persistence error.
        }
    }

    public static class ReportingProjectionPersistenceException extends RuntimeException {
        public ReportingProjectionPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
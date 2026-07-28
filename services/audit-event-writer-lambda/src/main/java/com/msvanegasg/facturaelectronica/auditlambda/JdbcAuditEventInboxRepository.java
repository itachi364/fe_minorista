package com.msvanegasg.facturaelectronica.auditlambda;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class JdbcAuditEventInboxRepository implements AuditEventInboxRepositoryPort {

    static final String CONSUMER_NAME = "audit-event-writer-lambda";

    private final DatabaseSettings settings;

    public JdbcAuditEventInboxRepository(DatabaseSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings is required");
    }

    @Override
    public boolean saveIfNew(DomainEventEnvelope envelope, AuditEventWriteRequest request) {
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
                insertAuditEvent(connection, request);
                connection.commit();
                return true;
            } catch (SQLException exception) {
                rollback(connection);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new AuditEventPersistenceException("Cannot persist audit event", exception);
        }
    }

    private int insertInbox(Connection connection, DomainEventEnvelope envelope) throws SQLException {
        String sql = """
                INSERT INTO audit_inbox_event (id, event_id, event_type, company_id, consumer_name, processed_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (event_id, consumer_name) DO NOTHING
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

    private void insertAuditEvent(Connection connection, AuditEventWriteRequest request) throws SQLException {
        String sql = """
                INSERT INTO audit_event (id, company_id, user_id, event_type, resource_type, resource_id, action, result,
                    detail, occurred_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, request.eventId());
            statement.setObject(2, request.companyId());
            if (request.userId() == null) {
                statement.setNull(3, Types.OTHER);
            } else {
                statement.setObject(3, request.userId());
            }
            statement.setString(4, request.eventType());
            statement.setString(5, request.resourceType());
            statement.setString(6, request.resourceId());
            statement.setString(7, request.action());
            statement.setString(8, request.result());
            statement.setObject(9, request.detail(), Types.OTHER);
            statement.setTimestamp(10, Timestamp.from(request.occurredAt()));
            statement.executeUpdate();
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // Preserve the original persistence error.
        }
    }

    public static class AuditEventPersistenceException extends RuntimeException {
        public AuditEventPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

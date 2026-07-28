package com.msvanegasg.facturaelectronica.inventorylambda;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;

public class JdbcInventorySaleEffectRepository implements InventorySaleEffectRepositoryPort {

    static final String CONSUMER_NAME = "inventory-sale-effect-lambda";

    private final DatabaseSettings settings;

    public JdbcInventorySaleEffectRepository(DatabaseSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings is required");
    }

    @Override
    public boolean applyIfNew(DomainEventEnvelope envelope, InventorySaleEffectRequest request) {
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
                for (InventorySaleLineEffect line : request.stockTrackedLines()) {
                    applyLine(connection, request, line);
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                rollback(connection);
                throw exception;
            }
        } catch (SQLException exception) {
            throw new InventorySaleEffectPersistenceException("Cannot apply inventory sale effect", exception);
        }
    }

    private int insertInbox(Connection connection, DomainEventEnvelope envelope) throws SQLException {
        String sql = """
                INSERT INTO inventory.inbox_event (id, event_id, event_type, company_id, consumer, processed_at)
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

    private void applyLine(Connection connection, InventorySaleEffectRequest request, InventorySaleLineEffect line)
            throws SQLException {
        String idempotencyKey = request.documentIdempotencyKey() + "-inventory-" + line.lineId();
        if (movementExists(connection, request.companyId(), request.saleId(), idempotencyKey)) {
            return;
        }
        StockSnapshot previous = lockStock(connection, request.companyId(), line.productId());
        BigDecimal resultingStock = previous.currentStock().subtract(line.quantity());
        if (resultingStock.signum() < 0) {
            throw new IllegalStateException("stock is insufficient for sale line " + line.lineId());
        }
        upsertStock(connection, request.companyId(), line.productId(), resultingStock, previous.reservedStock(),
                previous.averageCost(), request.occurredAt());
        insertMovement(connection, request, line, idempotencyKey, previous.currentStock(), resultingStock);
    }

    private boolean movementExists(Connection connection, UUID companyId, UUID saleId, String idempotencyKey)
            throws SQLException {
        String sql = """
                SELECT 1
                FROM inventory.inventory_movement
                WHERE company_id = ? AND source_document_type = 'SALE' AND source_document_id = ?
                    AND movement_type = 'SALE_OUT' AND idempotency_key = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companyId);
            statement.setObject(2, saleId);
            statement.setString(3, idempotencyKey);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private StockSnapshot lockStock(Connection connection, UUID companyId, UUID productId) throws SQLException {
        String sql = """
                SELECT current_stock, reserved_stock, average_cost
                FROM inventory.stock_balance
                WHERE company_id = ? AND product_id = ?
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companyId);
            statement.setObject(2, productId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new StockSnapshot(resultSet.getBigDecimal("current_stock"),
                            resultSet.getBigDecimal("reserved_stock"), resultSet.getBigDecimal("average_cost"));
                }
                return new StockSnapshot(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            }
        }
    }

    private void upsertStock(Connection connection, UUID companyId, UUID productId, BigDecimal currentStock,
            BigDecimal reservedStock, BigDecimal averageCost, Instant updatedAt) throws SQLException {
        String sql = """
                INSERT INTO inventory.stock_balance (company_id, product_id, current_stock, reserved_stock, average_cost, updated_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (company_id, product_id) DO UPDATE
                SET current_stock = EXCLUDED.current_stock,
                    reserved_stock = EXCLUDED.reserved_stock,
                    average_cost = EXCLUDED.average_cost,
                    updated_at = EXCLUDED.updated_at
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, companyId);
            statement.setObject(2, productId);
            statement.setBigDecimal(3, currentStock);
            statement.setBigDecimal(4, reservedStock);
            statement.setBigDecimal(5, averageCost);
            statement.setTimestamp(6, Timestamp.from(updatedAt));
            statement.executeUpdate();
        }
    }

    private void insertMovement(Connection connection, InventorySaleEffectRequest request, InventorySaleLineEffect line,
            String idempotencyKey, BigDecimal previousStock, BigDecimal resultingStock) throws SQLException {
        String sql = """
                INSERT INTO inventory.inventory_movement (id, company_id, product_id, movement_type, quantity, unit_cost,
                    previous_stock, resulting_stock, source_document_type, source_document_id, idempotency_key,
                    created_by, movement_at)
                VALUES (?, ?, ?, 'SALE_OUT', ?, ?, ?, ?, 'SALE', ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, UUID.randomUUID());
            statement.setObject(2, request.companyId());
            statement.setObject(3, line.productId());
            statement.setBigDecimal(4, line.quantity());
            statement.setBigDecimal(5, line.unitCost());
            statement.setBigDecimal(6, previousStock);
            statement.setBigDecimal(7, resultingStock);
            statement.setObject(8, request.saleId());
            statement.setString(9, idempotencyKey);
            statement.setNull(10, Types.OTHER);
            statement.setTimestamp(11, Timestamp.from(request.occurredAt()));
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

    private record StockSnapshot(BigDecimal currentStock, BigDecimal reservedStock, BigDecimal averageCost) {
    }

    public static class InventorySaleEffectPersistenceException extends RuntimeException {
        public InventorySaleEffectPersistenceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

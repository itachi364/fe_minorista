package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalLegalSourceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSourceEvent;

@Component
public class FiscalLegalSourceJdbcAdapter implements FiscalLegalSourceRepositoryPort {
    private static final String SOURCE_COLUMNS = "id, code, title, authority, official_url, issued_on, "
            + "review_due_on, active, created_at, created_by";
    private static final String EVENT_COLUMNS = "id, source_id, event_type, effective_from, effective_to, "
            + "reference, official_url, notes, created_at, created_by";

    private final JdbcTemplate jdbcTemplate;

    public FiscalLegalSourceJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<FiscalLegalSource> findAll() {
        return jdbcTemplate.query("SELECT " + SOURCE_COLUMNS + " FROM fiscal_legal_source ORDER BY code",
                FiscalLegalSourceJdbcAdapter::mapSource);
    }

    @Override
    public Optional<FiscalLegalSource> findById(UUID sourceId) {
        return jdbcTemplate.query("SELECT " + SOURCE_COLUMNS + " FROM fiscal_legal_source WHERE id = ?",
                FiscalLegalSourceJdbcAdapter::mapSource, sourceId).stream().findFirst();
    }

    @Override
    public List<FiscalLegalSourceEvent> findEvents(UUID sourceId) {
        return jdbcTemplate.query("SELECT " + EVENT_COLUMNS
                        + " FROM fiscal_legal_source_event WHERE source_id = ? ORDER BY effective_from, created_at",
                FiscalLegalSourceJdbcAdapter::mapEvent, sourceId);
    }

    @Override
    public FiscalLegalSource save(FiscalLegalSource source) {
        jdbcTemplate.update("INSERT INTO fiscal_legal_source "
                        + "(id, code, title, authority, official_url, issued_on, review_due_on, active, created_at, created_by) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                source.id(), source.code(), source.title(), source.authority(), source.officialUrl(),
                source.issuedOn(), source.reviewDueOn(), source.active(), source.createdAt(), source.createdBy());
        return source;
    }

    @Override
    public FiscalLegalSourceEvent saveEvent(FiscalLegalSourceEvent event) {
        jdbcTemplate.update("INSERT INTO fiscal_legal_source_event "
                        + "(id, source_id, event_type, effective_from, effective_to, reference, official_url, notes, created_at, created_by) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                event.id(), event.sourceId(), event.eventType().name(), event.effectiveFrom(), event.effectiveTo(),
                event.reference(), event.officialUrl(), event.notes(), event.createdAt(), event.createdBy());
        return event;
    }

    private static FiscalLegalSource mapSource(ResultSet rs, int rowNumber) throws SQLException {
        Date issuedOn = rs.getDate("issued_on");
        Date reviewDueOn = rs.getDate("review_due_on");
        Timestamp createdAt = rs.getTimestamp("created_at");
        return new FiscalLegalSource(rs.getObject("id", UUID.class), rs.getString("code"), rs.getString("title"),
                rs.getString("authority"), rs.getString("official_url"),
                issuedOn == null ? null : issuedOn.toLocalDate(), reviewDueOn == null ? null : reviewDueOn.toLocalDate(),
                rs.getBoolean("active"), createdAt.toInstant(), rs.getObject("created_by", UUID.class));
    }

    private static FiscalLegalSourceEvent mapEvent(ResultSet rs, int rowNumber) throws SQLException {
        Date effectiveTo = rs.getDate("effective_to");
        return new FiscalLegalSourceEvent(rs.getObject("id", UUID.class), rs.getObject("source_id", UUID.class),
                FiscalLegalEventType.valueOf(rs.getString("event_type")), rs.getDate("effective_from").toLocalDate(),
                effectiveTo == null ? null : effectiveTo.toLocalDate(), rs.getString("reference"),
                rs.getString("official_url"), rs.getString("notes"), rs.getTimestamp("created_at").toInstant(),
                rs.getObject("created_by", UUID.class));
    }
}

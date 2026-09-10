package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalEventCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalSourceCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalLegalSourcesUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalLegalSourceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCatalogWarning;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalEventType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSourceEvent;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalTimeline;

@Transactional
public class FiscalLegalSourceManagementService implements ManageFiscalLegalSourcesUseCase {
    private static final long WARNING_DAYS = 30;

    private final FiscalLegalSourceRepositoryPort repository;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public FiscalLegalSourceManagementService(FiscalLegalSourceRepositoryPort repository,
            IdGeneratorPort idGenerator, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiscalLegalTimeline> timelines(LocalDate evaluatedOn) {
        LocalDate date = evaluatedOn == null ? LocalDate.now(clock) : evaluatedOn;
        return repository.findAll().stream()
                .map(source -> {
                    List<FiscalLegalSourceEvent> events = repository.findEvents(source.id());
                    return new FiscalLegalTimeline(source, events, FiscalLegalTimeline.statusOn(events, date), date);
                })
                .sorted(Comparator.comparing(timeline -> timeline.source().code()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FiscalCatalogWarning> warnings(LocalDate evaluatedOn) {
        LocalDate date = evaluatedOn == null ? LocalDate.now(clock) : evaluatedOn;
        return timelines(date).stream().flatMap(timeline -> {
            FiscalLegalSource source = timeline.source();
            if (timeline.status() == FiscalLegalEventType.SUSPENDED
                    || timeline.status() == FiscalLegalEventType.REPEALED) {
                return java.util.stream.Stream.of(new FiscalCatalogWarning(source.id(), source.code(), "HIGH",
                        "LEGAL_SOURCE_" + timeline.status(),
                        "La fuente normativa no puede respaldar reglas activas en la fecha evaluada.", date));
            }
            if (source.reviewDueOn() == null) {
                return java.util.stream.Stream.of(new FiscalCatalogWarning(source.id(), source.code(), "MEDIUM",
                        "REVIEW_DATE_MISSING", "La fuente normativa no tiene fecha de proxima revision.", null));
            }
            if (!source.reviewDueOn().isAfter(date.plusDays(WARNING_DAYS))) {
                String severity = source.reviewDueOn().isBefore(date) ? "HIGH" : "MEDIUM";
                return java.util.stream.Stream.of(new FiscalCatalogWarning(source.id(), source.code(), severity,
                        "REVIEW_DUE", "La revision de la fuente normativa esta vencida o proxima a vencer.",
                        source.reviewDueOn()));
            }
            return java.util.stream.Stream.empty();
        }).toList();
    }

    @Override
    public FiscalLegalSource create(CreateFiscalLegalSourceCommand command) {
        Objects.requireNonNull(command, "command is required");
        requireText(command.code(), "code");
        requireText(command.title(), "title");
        requireText(command.authority(), "authority");
        validateHttps(command.officialUrl());
        if (command.reviewDueOn() != null && command.issuedOn() != null
                && command.reviewDueOn().isBefore(command.issuedOn())) {
            throw new IllegalArgumentException("reviewDueOn cannot be before issuedOn");
        }
        boolean duplicate = repository.findAll().stream()
                .anyMatch(source -> source.code().equalsIgnoreCase(command.code().trim()));
        if (duplicate) {
            throw new IllegalStateException("Ya existe una fuente normativa con ese codigo.");
        }
        FiscalLegalSource source = new FiscalLegalSource(idGenerator.newId(), command.code().trim().toUpperCase(Locale.ROOT),
                command.title().trim(), command.authority().trim(), command.officialUrl().trim(), command.issuedOn(),
                command.reviewDueOn(), true, Instant.now(clock), command.userId());
        return repository.save(source);
    }

    @Override
    public FiscalLegalSourceEvent addEvent(CreateFiscalLegalEventCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.sourceId(), "sourceId is required");
        Objects.requireNonNull(command.eventType(), "eventType is required");
        Objects.requireNonNull(command.effectiveFrom(), "effectiveFrom is required");
        requireText(command.reference(), "reference");
        validateHttps(command.officialUrl());
        if (command.effectiveTo() != null && command.effectiveTo().isBefore(command.effectiveFrom())) {
            throw new IllegalArgumentException("effectiveTo cannot be before effectiveFrom");
        }
        repository.findById(command.sourceId())
                .orElseThrow(() -> new IllegalStateException("La fuente normativa no existe."));
        FiscalLegalSourceEvent event = new FiscalLegalSourceEvent(idGenerator.newId(), command.sourceId(),
                command.eventType(), command.effectiveFrom(), command.effectiveTo(), command.reference().trim(),
                command.officialUrl().trim(), normalize(command.notes()), Instant.now(clock), command.userId());
        return repository.saveEvent(event);
    }

    private static void validateHttps(String value) {
        requireText(value, "officialUrl");
        if (!value.trim().toLowerCase(Locale.ROOT).startsWith("https://")) {
            throw new IllegalArgumentException("officialUrl must use HTTPS");
        }
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

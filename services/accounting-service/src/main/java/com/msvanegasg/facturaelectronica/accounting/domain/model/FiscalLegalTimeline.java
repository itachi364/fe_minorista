package com.msvanegasg.facturaelectronica.accounting.domain.model;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record FiscalLegalTimeline(
        FiscalLegalSource source,
        List<FiscalLegalSourceEvent> events,
        FiscalLegalEventType status,
        LocalDate evaluatedOn) {

    public FiscalLegalTimeline {
        events = events == null ? List.of() : List.copyOf(events);
    }

    public static FiscalLegalEventType statusOn(List<FiscalLegalSourceEvent> events, LocalDate date) {
        return events.stream()
                .filter(event -> event.appliesOn(date))
                .max(Comparator.comparing(FiscalLegalSourceEvent::effectiveFrom)
                        .thenComparingInt(event -> precedence(event.eventType())))
                .map(FiscalLegalSourceEvent::eventType)
                .orElse(null);
    }

    private static int precedence(FiscalLegalEventType type) {
        return switch (type) {
            case REPEALED -> 6;
            case REACTIVATED -> 5;
            case SUSPENDED -> 4;
            case MODIFIED -> 3;
            case EFFECTIVE -> 2;
            case PUBLISHED -> 1;
        };
    }
}

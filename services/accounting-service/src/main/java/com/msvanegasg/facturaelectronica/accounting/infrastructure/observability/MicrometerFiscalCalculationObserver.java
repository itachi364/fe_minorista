package com.msvanegasg.facturaelectronica.accounting.infrastructure.observability;

import java.time.Duration;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalCalculationObserver;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

import io.micrometer.core.instrument.MeterRegistry;

@Component
public class MicrometerFiscalCalculationObserver implements FiscalCalculationObserver {
    private final MeterRegistry registry;

    public MicrometerFiscalCalculationObserver(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void record(FiscalOperationType operationType, String status, long elapsedNanos) {
        String operation = operationType.name().toLowerCase(java.util.Locale.ROOT);
        String normalizedStatus = status.toLowerCase(java.util.Locale.ROOT);
        registry.counter("fiscal.calculations", "operation", operation, "status", normalizedStatus).increment();
        registry.timer("fiscal.calculation.duration", "operation", operation, "status", normalizedStatus)
                .record(Duration.ofNanos(elapsedNanos));
        if ("blocked".equals(normalizedStatus)) {
            registry.counter("fiscal.calculations.blocked", "operation", operation).increment();
        }
    }
}

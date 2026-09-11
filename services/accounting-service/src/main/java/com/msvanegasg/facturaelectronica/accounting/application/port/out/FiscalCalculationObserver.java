package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

public interface FiscalCalculationObserver {
    FiscalCalculationObserver NOOP = (operationType, status, elapsedNanos) -> { };

    void record(FiscalOperationType operationType, String status, long elapsedNanos);
}

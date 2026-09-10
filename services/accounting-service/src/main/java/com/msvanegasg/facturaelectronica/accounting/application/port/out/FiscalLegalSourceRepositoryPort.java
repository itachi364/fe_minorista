package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSourceEvent;

public interface FiscalLegalSourceRepositoryPort {
    List<FiscalLegalSource> findAll();
    Optional<FiscalLegalSource> findById(UUID sourceId);
    List<FiscalLegalSourceEvent> findEvents(UUID sourceId);
    FiscalLegalSource save(FiscalLegalSource source);
    FiscalLegalSourceEvent saveEvent(FiscalLegalSourceEvent event);
}

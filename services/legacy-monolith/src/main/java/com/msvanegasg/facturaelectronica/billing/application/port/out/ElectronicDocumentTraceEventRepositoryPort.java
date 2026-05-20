package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentTraceEvent;

public interface ElectronicDocumentTraceEventRepositoryPort {

    ElectronicDocumentTraceEvent save(ElectronicDocumentTraceEvent event);
}

package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.time.LocalDate;
import java.util.List;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalEventCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalSourceCommand;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCatalogWarning;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSourceEvent;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalTimeline;

public interface ManageFiscalLegalSourcesUseCase {
    List<FiscalLegalTimeline> timelines(LocalDate evaluatedOn);
    List<FiscalCatalogWarning> warnings(LocalDate evaluatedOn);
    FiscalLegalSource create(CreateFiscalLegalSourceCommand command);
    FiscalLegalSourceEvent addEvent(CreateFiscalLegalEventCommand command);
}

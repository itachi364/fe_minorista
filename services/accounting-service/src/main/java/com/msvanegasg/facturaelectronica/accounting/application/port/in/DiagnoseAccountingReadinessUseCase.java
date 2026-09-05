package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingReadinessResult;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingEventType;

public interface DiagnoseAccountingReadinessUseCase {

    AccountingReadinessResult diagnose(UUID companyId, AccountingEventType eventType);
}

package com.msvanegasg.facturaelectronica.accounting.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountingSetupResult;

public interface InitializeBasicAccountingSetupUseCase {

    AccountingSetupResult initialize(UUID companyId);
}
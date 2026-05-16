package com.msvanegasg.facturaelectronica.billing.infrastructure.transaction;

import java.util.Objects;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;

public class TransactionalAssignFiscalNumberUseCase implements AssignFiscalNumberUseCase {

    private final AssignFiscalNumberUseCase delegate;

    public TransactionalAssignFiscalNumberUseCase(AssignFiscalNumberUseCase delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    @Transactional
    public FiscalNumberResult assign(AssignFiscalNumberCommand command) {
        return delegate.assign(command);
    }
}

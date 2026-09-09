package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CompanyFiscalPolicyCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CompanyFiscalPolicyResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageCompanyFiscalPolicyUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.CompanyFiscalPolicyRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InvoicingObligationPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.CompanyFiscalPolicy;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public class CompanyFiscalPolicyService implements ManageCompanyFiscalPolicyUseCase {

    private final CompanyFiscalPolicyRepositoryPort repository;
    private final ClockPort clock;
    private final InvoicingObligationPort invoicingObligation;

    public CompanyFiscalPolicyService(CompanyFiscalPolicyRepositoryPort repository, ClockPort clock) {
        this(repository, clock, InvoicingObligationPort.allowAll());
    }

    public CompanyFiscalPolicyService(CompanyFiscalPolicyRepositoryPort repository, ClockPort clock,
            InvoicingObligationPort invoicingObligation) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
        this.invoicingObligation = Objects.requireNonNull(invoicingObligation);
    }

    @Override
    public CompanyFiscalPolicyResult findByCompanyId(UUID companyId) {
        Objects.requireNonNull(companyId, "companyId is required");
        return toResult(repository.findByCompanyId(companyId).orElseGet(() -> CompanyFiscalPolicy.defaults(companyId)));
    }

    @Override
    public CompanyFiscalPolicyResult configure(CompanyFiscalPolicyCommand command) {
        Objects.requireNonNull(command, "command is required");
        if ((command.defaultSaleDocumentType() == null
                || command.defaultSaleDocumentType() == ElectronicDocumentType.NON_FISCAL_SALE)
                && !invoicingObligation.allowsNonFiscalSale(command.companyId())) {
            throw new IllegalStateException("La empresa no tiene una clasificacion vigente que permita venta no fiscal.");
        }
        CompanyFiscalPolicy policy = CompanyFiscalPolicy.configure(command.companyId(),
                command.defaultSaleDocumentType(), command.allowDocumentTypeOverride(),
                command.requirePinForOverride(), clock.now());
        return toResult(repository.save(policy));
    }

    static CompanyFiscalPolicyResult toResult(CompanyFiscalPolicy policy) {
        return new CompanyFiscalPolicyResult(policy.companyId(), policy.defaultSaleDocumentType(),
                policy.allowDocumentTypeOverride(), policy.requirePinForOverride(), policy.updatedAt());
    }
}

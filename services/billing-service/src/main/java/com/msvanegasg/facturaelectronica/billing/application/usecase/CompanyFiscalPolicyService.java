package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CompanyFiscalPolicyCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CompanyFiscalPolicyResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageCompanyFiscalPolicyUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.CompanyFiscalPolicyRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.CompanyFiscalPolicy;

public class CompanyFiscalPolicyService implements ManageCompanyFiscalPolicyUseCase {

    private final CompanyFiscalPolicyRepositoryPort repository;
    private final ClockPort clock;

    public CompanyFiscalPolicyService(CompanyFiscalPolicyRepositoryPort repository, ClockPort clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public CompanyFiscalPolicyResult findByCompanyId(UUID companyId) {
        Objects.requireNonNull(companyId, "companyId is required");
        return toResult(repository.findByCompanyId(companyId).orElseGet(() -> CompanyFiscalPolicy.defaults(companyId)));
    }

    @Override
    public CompanyFiscalPolicyResult configure(CompanyFiscalPolicyCommand command) {
        Objects.requireNonNull(command, "command is required");
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

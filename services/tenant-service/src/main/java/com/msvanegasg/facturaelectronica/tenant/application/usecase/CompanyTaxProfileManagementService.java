package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyTaxProfileUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyTaxProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyTaxProfile;

public class CompanyTaxProfileManagementService implements ManageCompanyTaxProfileUseCase {
    private final CompanyRepositoryPort companyRepository;
    private final CompanyTaxProfileRepositoryPort profileRepository;
    private final ClockPort clock;

    public CompanyTaxProfileManagementService(CompanyRepositoryPort companyRepository,
            CompanyTaxProfileRepositoryPort profileRepository, ClockPort clock) {
        this.companyRepository = companyRepository;
        this.profileRepository = profileRepository;
        this.clock = clock;
    }

    @Override
    public CompanyTaxProfileResult findByCompanyId(UUID companyId) {
        assertCompanyExists(companyId);
        return profileRepository.findByCompanyId(companyId)
                .map(CompanyTaxProfileResult::from)
                .orElseThrow(() -> new CompanyTaxProfileNotFoundException(companyId));
    }

    @Override
    public CompanyTaxProfileResult findByCompanyId(UUID companyId, LocalDate effectiveOn) {
        assertCompanyExists(companyId);
        if (effectiveOn == null) {
            return findByCompanyId(companyId);
        }
        return profileRepository.findEffective(companyId, effectiveOn)
                .map(CompanyTaxProfileResult::from)
                .orElseThrow(() -> new CompanyTaxProfileNotFoundException(companyId));
    }

    @Override
    public CompanyTaxProfileResult update(UUID companyId, CompanyTaxProfileCommand command) {
        assertCompanyExists(companyId);
        CompanyTaxProfile profile = new CompanyTaxProfile(companyId, command.companySize(),
                command.financialReportingGroup(), command.taxRegime(), command.rutResponsibilities(),
                command.vatResponsible(), command.withholdingAgent(), command.vatWithholdingAgent(),
                command.icaWithholdingAgent(), command.largeTaxpayer(), command.selfWithholding(),
                command.simpleRegime(), command.icaMunicipalityCode(), command.ciiuCodes(), command.updatedBy(),
                clock.now());
        return CompanyTaxProfileResult.from(profileRepository.save(profile));
    }

    private void assertCompanyExists(UUID companyId) {
        companyRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException(companyId));
    }
}

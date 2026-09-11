package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CreateCompanyCommand;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyTaxProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyTaxProfile;

public class CompanyManagementService implements ManageCompanyUseCase {

    private final CompanyRepositoryPort companyRepository;
    private final CompanyTaxProfileRepositoryPort taxProfileRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public CompanyManagementService(
            CompanyRepositoryPort companyRepository,
            CompanyTaxProfileRepositoryPort taxProfileRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.companyRepository = companyRepository;
        this.taxProfileRepository = taxProfileRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    @Transactional
    public CompanyResult create(CreateCompanyCommand command) {
        if (companyRepository.existsByIdentification(command.identificationTypeCode(), command.identificationNumber())) {
            throw new CompanyAlreadyExistsException(command.identificationNumber());
        }
        UUID companyId = idGenerator.nextId();
        Instant now = clock.now();
        Company company = Company.create(
                companyId,
                command.legalName(),
                command.tradeName(),
                command.identificationTypeCode(),
                command.identificationNumber(),
                command.verificationDigit(),
                command.email(),
                now);
        CompanyTaxProfile profile = toTaxProfile(companyId, command, now);
        CompanyResult result = CompanyResult.from(companyRepository.save(company));
        taxProfileRepository.save(profile);
        return result;
    }

    @Override
    @Transactional
    public CompanyResult update(UUID companyId, CreateCompanyCommand command) {
        Company current = findCompany(companyId);
        boolean identificationChanged = !current.identificationTypeCode().equals(command.identificationTypeCode())
                || !current.identificationNumber().equals(command.identificationNumber());
        if (identificationChanged
                && companyRepository.existsByIdentification(command.identificationTypeCode(), command.identificationNumber())) {
            throw new CompanyAlreadyExistsException(command.identificationNumber());
        }
        Instant now = clock.now();
        Company updated = current.update(command.legalName(), command.tradeName(), command.identificationTypeCode(),
                command.identificationNumber(), command.verificationDigit(), command.email(), now);
        CompanyTaxProfile profile = toTaxProfile(companyId, command, now);
        CompanyResult result = CompanyResult.from(companyRepository.save(updated));
        taxProfileRepository.save(profile);
        return result;
    }

    @Override
    public List<CompanyResult> list() {
        return companyRepository.findAll().stream()
                .map(CompanyResult::from)
                .toList();
    }

    @Override
    public CompanyResult findById(UUID companyId) {
        return CompanyResult.from(findCompany(companyId));
    }

    @Override
    public CompanyResult activate(UUID companyId) {
        Company company = findCompany(companyId);
        Instant now = clock.now();
        return CompanyResult.from(companyRepository.save(company.activate(now)));
    }

    @Override
    public CompanyResult suspend(UUID companyId) {
        Company company = findCompany(companyId);
        Instant now = clock.now();
        return CompanyResult.from(companyRepository.save(company.suspend(now)));
    }

    private Company findCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));
    }

    private static CompanyTaxProfile toTaxProfile(UUID companyId, CreateCompanyCommand command, Instant now) {
        var profile = command.taxProfile();
        if (profile == null) {
            throw new IllegalArgumentException("Company tax profile is required");
        }
        return new CompanyTaxProfile(companyId, profile.companySize(), profile.financialReportingGroup(),
                profile.taxRegime(), profile.rutResponsibilities(), profile.vatResponsible(),
                profile.withholdingAgent(), profile.vatWithholdingAgent(), profile.icaWithholdingAgent(),
                profile.largeTaxpayer(), profile.selfWithholding(), profile.simpleRegime(),
                profile.icaMunicipalityCode(), profile.ciiuCodes(), profile.taxResidency(),
                profile.incomeTaxStatus(), profile.selfWithholdingScopes(), profile.fiscalEvidenceReference(),
                profile.updatedBy(), now);
    }
}

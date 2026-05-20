package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CreateCompanyCommand;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;

public class CompanyManagementService implements ManageCompanyUseCase {

    private final CompanyRepositoryPort companyRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public CompanyManagementService(
            CompanyRepositoryPort companyRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.companyRepository = companyRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public CompanyResult create(CreateCompanyCommand command) {
        if (companyRepository.existsByIdentification(command.identificationTypeId(), command.identificationNumber())) {
            throw new CompanyAlreadyExistsException(command.identificationNumber());
        }
        Company company = Company.create(
                idGenerator.nextId(),
                command.legalName(),
                command.tradeName(),
                command.identificationTypeId(),
                command.identificationNumber(),
                command.verificationDigit(),
                command.email(),
                clock.now());
        return CompanyResult.from(companyRepository.save(company));
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
}

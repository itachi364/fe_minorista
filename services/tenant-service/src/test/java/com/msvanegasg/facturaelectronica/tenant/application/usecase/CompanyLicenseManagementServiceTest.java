package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseValidationResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyLicenseRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicense;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;

class CompanyLicenseManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LICENSE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID IDENTIFICATION_TYPE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private final InMemoryCompanyRepository companyRepository = new InMemoryCompanyRepository();
    private final InMemoryCompanyLicenseRepository licenseRepository = new InMemoryCompanyLicenseRepository();
    private final CompanyLicenseManagementService service = new CompanyLicenseManagementService(
            companyRepository,
            licenseRepository,
            fixedId(),
            fixedClock());

    @BeforeEach
    void setUp() {
        companyRepository.save(company());
    }

    @Test
    void createsActiveLicense() {
        CompanyLicenseResult result = service.save(COMPANY_ID, command("SMALL_BUSINESS", LocalDate.parse("2027-05-19")));

        assertThat(result.id()).isEqualTo(LICENSE_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.status()).isEqualTo(CompanyLicenseStatus.ACTIVE);
        assertThat(result.maxUsers()).isEqualTo(5);
    }

    @Test
    void updatesExistingLicense() {
        service.save(COMPANY_ID, command("SMALL_BUSINESS", LocalDate.parse("2027-05-19")));

        CompanyLicenseResult result = service.save(COMPANY_ID, command("GROWTH", LocalDate.parse("2028-05-19")));

        assertThat(result.id()).isEqualTo(LICENSE_ID);
        assertThat(result.planCode()).isEqualTo("GROWTH");
        assertThat(result.validTo()).isEqualTo(LocalDate.parse("2028-05-19"));
    }

    @Test
    void allowsBusinessActionWhenLicenseIsActiveAndCurrent() {
        service.save(COMPANY_ID, command("SMALL_BUSINESS", LocalDate.parse("2027-05-19")));

        CompanyLicenseValidationResult result = service.validate(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT);

        assertThat(result.allowed()).isTrue();
        assertThat(result.status()).isEqualTo(CompanyLicenseStatus.ACTIVE);
        assertThat(result.reasonCode()).isEqualTo("LICENSE_ACTIVE");
    }

    @Test
    void blocksBusinessActionWhenLicenseIsSuspended() {
        service.save(COMPANY_ID, command("SMALL_BUSINESS", LocalDate.parse("2027-05-19")));
        service.suspend(COMPANY_ID);

        CompanyLicenseValidationResult result = service.validate(COMPANY_ID, LicenseAction.CREATE_TRANSACTION);

        assertThat(result.allowed()).isFalse();
        assertThat(result.status()).isEqualTo(CompanyLicenseStatus.SUSPENDED);
        assertThat(result.reasonCode()).isEqualTo("LICENSE_SUSPENDED");
    }

    @Test
    void blocksBusinessActionWhenLicenseIsExpired() {
        service.save(COMPANY_ID, command("SMALL_BUSINESS", LocalDate.parse("2026-05-18")));

        CompanyLicenseValidationResult result = service.validate(COMPANY_ID, LicenseAction.ISSUE_FISCAL_DOCUMENT);

        assertThat(result.allowed()).isFalse();
        assertThat(result.status()).isEqualTo(CompanyLicenseStatus.EXPIRED);
        assertThat(result.reasonCode()).isEqualTo("LICENSE_EXPIRED");
    }

    @Test
    void throwsWhenCompanyDoesNotExist() {
        UUID missingCompanyId = UUID.fromString("99999999-9999-9999-9999-999999999999");

        assertThatThrownBy(() -> service.save(missingCompanyId,
                command("SMALL_BUSINESS", LocalDate.parse("2027-05-19"))))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void throwsWhenLicenseDoesNotExist() {
        assertThatThrownBy(() -> service.validate(COMPANY_ID, LicenseAction.CREATE_USER))
                .isInstanceOf(CompanyLicenseNotFoundException.class);
    }

    private static CompanyLicenseCommand command(String planCode, LocalDate validTo) {
        return new CompanyLicenseCommand(planCode, LocalDate.parse("2026-05-01"), validTo, 5, 1000);
    }

    private static Company company() {
        return new Company(COMPANY_ID, "Mi Empresa SAS", "Mi Tienda", IDENTIFICATION_TYPE_ID, "900123456", "7",
                "admin@example.com", CompanyStatus.ACTIVE, NOW, NOW);
    }

    private static IdGeneratorPort fixedId() {
        return () -> LICENSE_ID;
    }

    private static ClockPort fixedClock() {
        return () -> NOW;
    }

    private static class InMemoryCompanyRepository implements CompanyRepositoryPort {

        private final Map<UUID, Company> companies = new HashMap<>();

        @Override
        public Company save(Company company) {
            companies.put(company.id(), company);
            return company;
        }

        @Override
        public Optional<Company> findById(UUID id) {
            return Optional.ofNullable(companies.get(id));
        }

        @Override
        public boolean existsByIdentification(UUID identificationTypeId, String identificationNumber) {
            return companies.values().stream()
                    .anyMatch(company -> company.identificationTypeId().equals(identificationTypeId)
                            && company.identificationNumber().equals(identificationNumber));
        }
    }

    private static class InMemoryCompanyLicenseRepository implements CompanyLicenseRepositoryPort {

        private final Map<UUID, CompanyLicense> licensesByCompany = new HashMap<>();

        @Override
        public CompanyLicense save(CompanyLicense license) {
            licensesByCompany.put(license.companyId(), license);
            return license;
        }

        @Override
        public Optional<CompanyLicense> findByCompanyId(UUID companyId) {
            return Optional.ofNullable(licensesByCompany.get(companyId));
        }
    }
}

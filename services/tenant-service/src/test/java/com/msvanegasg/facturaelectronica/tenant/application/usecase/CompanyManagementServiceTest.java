package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CreateCompanyCommand;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyStatus;

class CompanyManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Integer IDENTIFICATION_TYPE_CODE = 31;
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    private final InMemoryCompanyRepository repository = new InMemoryCompanyRepository();
    private final CompanyManagementService service = new CompanyManagementService(
            repository,
            fixedId(),
            fixedClock());

    @Test
    void createsActiveCompany() {
        CompanyResult result = service.create(command("900123456"));

        assertThat(result.id()).isEqualTo(COMPANY_ID);
        assertThat(result.status()).isEqualTo(CompanyStatus.ACTIVE);
        assertThat(result.createdAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsDuplicatedIdentification() {
        service.create(command("900123456"));

        assertThatThrownBy(() -> service.create(command("900123456")))
                .isInstanceOf(CompanyAlreadyExistsException.class);
    }

    @Test
    void suspendsAndActivatesCompany() {
        CompanyResult created = service.create(command("900123456"));

        CompanyResult suspended = service.suspend(created.id());
        CompanyResult activated = service.activate(created.id());

        assertThat(suspended.status()).isEqualTo(CompanyStatus.SUSPENDED);
        assertThat(activated.status()).isEqualTo(CompanyStatus.ACTIVE);
    }

    @Test
    void updatesCompanyKeepingStatusAndCreatedAt() {
        CompanyResult created = service.create(command("900123456"));

        CompanyResult updated = service.update(created.id(), new CreateCompanyCommand(
                "Mi Empresa Actualizada SAS",
                "Tienda Actualizada",
                IDENTIFICATION_TYPE_CODE,
                "900123456",
                "7",
                "nuevo@example.com"));

        assertThat(updated.legalName()).isEqualTo("Mi Empresa Actualizada SAS");
        assertThat(updated.tradeName()).isEqualTo("Tienda Actualizada");
        assertThat(updated.email()).isEqualTo("nuevo@example.com");
        assertThat(updated.status()).isEqualTo(CompanyStatus.ACTIVE);
        assertThat(updated.createdAt()).isEqualTo(NOW);
        assertThat(updated.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void throwsWhenCompanyDoesNotExist() {
        assertThatThrownBy(() -> service.findById(COMPANY_ID))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    private static CreateCompanyCommand command(String identificationNumber) {
        return new CreateCompanyCommand(
                "Mi Empresa SAS",
                "Mi Tienda",
                IDENTIFICATION_TYPE_CODE,
                identificationNumber,
                "7",
                "admin@example.com");
    }

    private static IdGeneratorPort fixedId() {
        return () -> COMPANY_ID;
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
        public List<Company> findAll() {
            return List.copyOf(companies.values());
        }



        @Override
        public boolean existsByIdentification(Integer identificationTypeCode, String identificationNumber) {
            return companies.values().stream()
                    .anyMatch(company -> company.identificationTypeCode().equals(identificationTypeCode)
                            && company.identificationNumber().equals(identificationNumber));
        }
    }
}

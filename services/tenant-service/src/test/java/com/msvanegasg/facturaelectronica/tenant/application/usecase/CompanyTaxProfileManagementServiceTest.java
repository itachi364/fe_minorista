package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyTaxProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyTaxProfile;

class CompanyTaxProfileManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-09-08T10:00:00Z");

    private final InMemoryProfileRepository profileRepository = new InMemoryProfileRepository();
    private final CompanyTaxProfileManagementService service = new CompanyTaxProfileManagementService(
            new ExistingCompanyRepository(), profileRepository, () -> NOW);

    @Test
    void persistsAndReadsNormalizedFiscalProfileWithMultipleCiiuCodes() {
        var result = service.update(COMPANY_ID, new CompanyTaxProfileCommand("micro", "grupo_3", "ordinario",
                Set.of("o-13", "O-23"), true, true, true, true, true, false, false, "11001",
                Set.of("6201", "4711"), USER_ID));

        assertThat(result.taxRegime()).isEqualTo("ORDINARIO");
        assertThat(result.rutResponsibilities()).containsExactlyInAnyOrder("O-13", "O-23");
        assertThat(result.ciiuCodes()).containsExactlyInAnyOrder("6201", "4711");
        assertThat(result.icaMunicipalityCode()).isEqualTo("11001");
        assertThat(service.findByCompanyId(COMPANY_ID)).isEqualTo(result);
    }

    @Test
    void rejectsSimpleFlagForNonSimpleRegime() {
        CompanyTaxProfileCommand command = new CompanyTaxProfileCommand("MICRO", "GRUPO_3", "ORDINARIO",
                Set.of(), false, false, false, false, false, false, true, null, Set.of(), USER_ID);

        assertThatThrownBy(() -> service.update(COMPANY_ID, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SIMPLE");
    }

    private static final class InMemoryProfileRepository implements CompanyTaxProfileRepositoryPort {
        private CompanyTaxProfile profile;

        @Override
        public Optional<CompanyTaxProfile> findByCompanyId(UUID companyId) {
            return Optional.ofNullable(profile).filter(value -> value.companyId().equals(companyId));
        }

        @Override
        public CompanyTaxProfile save(CompanyTaxProfile profile) {
            this.profile = profile;
            return profile;
        }
    }

    private static final class ExistingCompanyRepository implements CompanyRepositoryPort {
        private final Company company = Company.create(COMPANY_ID, "Empresa SAS", "Empresa", 31, "900123456", "7",
                "admin@example.com", NOW);

        @Override
        public Company save(Company company) {
            return company;
        }

        @Override
        public Optional<Company> findById(UUID id) {
            return COMPANY_ID.equals(id) ? Optional.of(company) : Optional.empty();
        }

        @Override
        public List<Company> findAll() {
            return List.of(company);
        }

        @Override
        public boolean existsByIdentification(Integer identificationTypeCode, String identificationNumber) {
            return false;
        }
    }
}

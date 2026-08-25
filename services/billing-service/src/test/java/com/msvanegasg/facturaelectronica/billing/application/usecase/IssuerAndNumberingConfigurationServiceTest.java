package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ConfigureIssuerProfileCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreateNumberingResolutionCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;

class IssuerAndNumberingConfigurationServiceTest {

    private static final UUID ISSUER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID ISSUER_ID_2 = UUID.fromString("33333333-3333-3333-3333-333333333334");
    private static final UUID RESOLUTION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID RESOLUTION_ID_2 = UUID.fromString("44444444-4444-4444-4444-444444444445");
    private static final UUID COMPANY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final LocalDate DOCUMENT_DATE = LocalDate.of(2026, 5, 20);

    @Test
    void configuresActiveIssuerProfile() {
        InMemoryIssuerProfileRepository repository = new InMemoryIssuerProfileRepository();
        var service = new ConfigureIssuerProfileService(repository, () -> ISSUER_ID);

        var result = service.configure(new ConfigureIssuerProfileCommand(COMPANY_ID, "ACME SAS", "900123456", "7",
                List.of("O-13"), "11001", "Calle 1 # 2-3"));

        assertThat(result.id()).isEqualTo(ISSUER_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.taxResponsibilities()).containsExactly("O-13");
        assertThat(repository.findActiveByCompanyId(COMPANY_ID)).isPresent();
    }

    @Test
    void configuringIssuerKeepsOnlyOneActiveIssuerPerCompany() {
        InMemoryIssuerProfileRepository repository = new InMemoryIssuerProfileRepository();

        repository.saveAsOnlyActive(IssuerProfile.configure(ISSUER_ID, COMPANY_ID, "ACME SAS", "900123456", "7",
                List.of("O-13"), "11001", "Calle 1 # 2-3"));
        repository.saveAsOnlyActive(IssuerProfile.configure(ISSUER_ID_2, COMPANY_ID, "ACME Renovada SAS",
                "900123456", "7", List.of("O-13"), "11001", "Calle 4 # 5-6"));

        assertThat(repository.findByCompanyId(COMPANY_ID)).hasSize(2);
        assertThat(repository.findByCompanyIdAndId(COMPANY_ID, ISSUER_ID)).get().extracting(IssuerProfile::active)
                .isEqualTo(false);
        assertThat(repository.findActiveByCompanyId(COMPANY_ID)).get().extracting(IssuerProfile::id)
                .isEqualTo(ISSUER_ID_2);
    }

    @Test
    void createsResolutionStartingBeforeFirstAuthorizedNumber() {
        var service = new CreateNumberingResolutionService(new InMemoryNumberingResolutionRepository(),
                () -> RESOLUTION_ID);

        var result = service.create(new CreateNumberingResolutionCommand(COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS, "18760000001", "pos", 100, 200,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), FiscalEnvironment.TEST));

        assertThat(result.prefix()).isEqualTo("POS");
        assertThat(result.currentNumber()).isEqualTo(99);
        assertThat(result.active()).isTrue();
    }

    @Test
    void activatingResolutionKeepsOnlyOneActiveResolutionForDocumentTypeAndEnvironment() {
        InMemoryNumberingResolutionRepository repository = new InMemoryNumberingResolutionRepository();
        NumberingResolution first = repository.saveAsOnlyActive(NumberingResolution.create(RESOLUTION_ID, COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS, "18760000001", "POS", 100, 200,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), FiscalEnvironment.TEST));
        repository.saveAsOnlyActive(NumberingResolution.create(RESOLUTION_ID_2, COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS, "18760000002", "PE2", 201, 300,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), FiscalEnvironment.TEST));

        assertThat(repository.findByCompanyIdAndId(COMPANY_ID, first.id())).get()
                .extracting(NumberingResolution::active).isEqualTo(false);
        assertThat(repository.findActiveResolution(COMPANY_ID, ElectronicDocumentType.ELECTRONIC_POS,
                FiscalEnvironment.TEST, DOCUMENT_DATE)).get().extracting(NumberingResolution::id)
                .isEqualTo(RESOLUTION_ID_2);
    }

    @Test
    void rejectsInvalidResolutionPrefix() {
        var service = new CreateNumberingResolutionService(new InMemoryNumberingResolutionRepository(),
                () -> RESOLUTION_ID);

        assertThatThrownBy(() -> service.create(new CreateNumberingResolutionCommand(COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS, "18760000001", "POS01", 1, 10,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), FiscalEnvironment.TEST)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("prefix must be alphanumeric and up to 4 characters");
    }

    @Test
    void assignFiscalNumberRequiresActiveIssuer() {
        var service = new AssignFiscalNumberService(new InMemoryIssuerProfileRepository(),
                new InMemoryNumberingResolutionRepository());

        assertThatThrownBy(() -> service.assign(new AssignFiscalNumberCommand(COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS, DOCUMENT_DATE, FiscalEnvironment.TEST)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Debes configurar un emisor fiscal activo antes de confirmar ventas POS.");
    }

    @Test
    void assignFiscalNumberRequiresActiveNumberingResolution() {
        InMemoryIssuerProfileRepository issuers = new InMemoryIssuerProfileRepository();
        issuers.save(IssuerProfile.configure(ISSUER_ID, COMPANY_ID, "ACME SAS", "900123456", "7", List.of(),
                "11001", "Calle 1 # 2-3"));
        var service = new AssignFiscalNumberService(issuers, new InMemoryNumberingResolutionRepository());

        assertThatThrownBy(() -> service.assign(new AssignFiscalNumberCommand(COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS, DOCUMENT_DATE, FiscalEnvironment.TEST)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Debes configurar una resolucion de numeracion activa para POS electronico antes de confirmar ventas.");
    }

    @Test
    void assignFiscalNumberAdvancesConsecutive() {
        InMemoryIssuerProfileRepository issuers = new InMemoryIssuerProfileRepository();
        issuers.save(IssuerProfile.configure(ISSUER_ID, COMPANY_ID, "ACME SAS", "900123456", "7", List.of(),
                "11001", "Calle 1 # 2-3"));
        InMemoryNumberingResolutionRepository resolutions = new InMemoryNumberingResolutionRepository();
        resolutions.save(NumberingResolution.create(RESOLUTION_ID, COMPANY_ID, ElectronicDocumentType.ELECTRONIC_POS,
                "18760000001", "POS", 50, 51, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31),
                FiscalEnvironment.TEST));
        var service = new AssignFiscalNumberService(issuers, resolutions);

        var first = service.assign(new AssignFiscalNumberCommand(COMPANY_ID, ElectronicDocumentType.ELECTRONIC_POS,
                DOCUMENT_DATE, FiscalEnvironment.TEST));
        var second = service.assign(new AssignFiscalNumberCommand(COMPANY_ID, ElectronicDocumentType.ELECTRONIC_POS,
                DOCUMENT_DATE, FiscalEnvironment.TEST));

        assertThat(first.prefix()).isEqualTo("POS");
        assertThat(first.number()).isEqualTo(50);
        assertThat(second.number()).isEqualTo(51);
        assertThat(resolutions.savedResolution().currentNumber()).isEqualTo(51);
    }

    private static final class InMemoryIssuerProfileRepository implements IssuerProfileRepositoryPort {
        private final Map<UUID, IssuerProfile> issuerProfiles = new HashMap<>();

        @Override
        public IssuerProfile save(IssuerProfile issuerProfile) {
            issuerProfiles.put(issuerProfile.id(), issuerProfile);
            return issuerProfile;
        }

        @Override
        public IssuerProfile saveAsOnlyActive(IssuerProfile issuerProfile) {
            issuerProfiles.replaceAll((id, current) -> current.companyId().equals(issuerProfile.companyId())
                    && !current.id().equals(issuerProfile.id()) ? current.deactivate() : current);
            return save(issuerProfile);
        }

        @Override
        public Optional<IssuerProfile> findActiveByCompanyId(UUID companyId) {
            return issuerProfiles.values().stream()
                    .filter(issuer -> issuer.companyId().equals(companyId) && issuer.active())
                    .findFirst();
        }

        @Override
        public Optional<IssuerProfile> findByCompanyIdAndId(UUID companyId, UUID issuerId) {
            return Optional.ofNullable(issuerProfiles.get(issuerId))
                    .filter(issuer -> issuer.companyId().equals(companyId));
        }

        @Override
        public List<IssuerProfile> findByCompanyId(UUID companyId) {
            return issuerProfiles.values().stream()
                    .filter(issuer -> issuer.companyId().equals(companyId))
                    .toList();
        }
    }

    private static final class InMemoryNumberingResolutionRepository implements NumberingResolutionRepositoryPort {
        private final Map<UUID, NumberingResolution> resolutions = new HashMap<>();
        private NumberingResolution savedResolution;

        @Override
        public NumberingResolution save(NumberingResolution numberingResolution) {
            resolutions.put(numberingResolution.id(), numberingResolution);
            savedResolution = numberingResolution;
            return numberingResolution;
        }

        @Override
        public NumberingResolution saveAsOnlyActive(NumberingResolution numberingResolution) {
            resolutions.replaceAll((id, current) -> sameActivationScope(current, numberingResolution)
                    && !current.id().equals(numberingResolution.id()) ? current.deactivate() : current);
            return save(numberingResolution);
        }

        @Override
        public Optional<NumberingResolution> findActiveResolution(UUID companyId, ElectronicDocumentType documentType,
                FiscalEnvironment environment, LocalDate documentDate) {
            return resolutions.values().stream()
                    .filter(resolution -> resolution.isAvailableFor(companyId, documentType, documentDate, environment))
                    .findFirst();
        }

        @Override
        public List<NumberingResolution> findByCompanyId(UUID companyId, ElectronicDocumentType documentType,
                Boolean active) {
            return resolutions.values().stream().filter(resolution -> resolution.companyId().equals(companyId))
                    .filter(resolution -> documentType == null || resolution.documentType() == documentType)
                    .filter(resolution -> active == null || resolution.active() == active)
                    .toList();
        }

        @Override
        public Optional<NumberingResolution> findByCompanyIdAndId(UUID companyId, UUID resolutionId) {
            return Optional.ofNullable(resolutions.get(resolutionId))
                    .filter(resolution -> resolution.companyId().equals(companyId));
        }

        NumberingResolution savedResolution() {
            return savedResolution;
        }

        private static boolean sameActivationScope(NumberingResolution current, NumberingResolution candidate) {
            return current.companyId().equals(candidate.companyId())
                    && current.documentType() == candidate.documentType()
                    && current.environment() == candidate.environment();
        }
    }
}

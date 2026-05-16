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
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.NumberingResolutionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.NumberingResolutionRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;

class IssuerAndNumberingConfigurationServiceTest {

    private static final UUID ISSUER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID RESOLUTION_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID COMPANY_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final LocalDate DOCUMENT_DATE = LocalDate.of(2026, 5, 11);

    @Test
    void configureIssuerProfilePersistsActiveFiscalInformation() {
        InMemoryIssuerProfileRepository issuerRepository = new InMemoryIssuerProfileRepository();
        ConfigureIssuerProfileService service = new ConfigureIssuerProfileService(
                issuerRepository,
                () -> ISSUER_ID);
        ConfigureIssuerProfileCommand command = new ConfigureIssuerProfileCommand(
                COMPANY_ID,
                "ACME SAS",
                "900123456",
                "7",
                List.of("O-13"),
                "11001",
                "Calle 1 # 2-3");

        IssuerProfileResult result = service.configure(command);

        assertThat(result.id()).isEqualTo(ISSUER_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.legalName()).isEqualTo("ACME SAS");
        assertThat(result.nit()).isEqualTo("900123456");
        assertThat(result.verificationDigit()).isEqualTo("7");
        assertThat(result.taxResponsibilities()).containsExactly("O-13");
        assertThat(result.active()).isTrue();
        assertThat(issuerRepository.findActiveByCompanyId(COMPANY_ID)).isPresent();
    }

    @Test
    void createNumberingResolutionRejectsInvalidPrefix() {
        CreateNumberingResolutionService service = new CreateNumberingResolutionService(
                new InMemoryNumberingResolutionRepository(),
                () -> RESOLUTION_ID);
        CreateNumberingResolutionCommand command = new CreateNumberingResolutionCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                "18760000001",
                "POS01",
                1,
                10,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                FiscalEnvironment.TEST);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("prefix must be alphanumeric and up to 4 characters");
    }

    @Test
    void createNumberingResolutionStartsBeforeFirstAuthorizedNumber() {
        CreateNumberingResolutionService service = new CreateNumberingResolutionService(
                new InMemoryNumberingResolutionRepository(),
                () -> RESOLUTION_ID);
        CreateNumberingResolutionCommand command = new CreateNumberingResolutionCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_INVOICE,
                "18760000002",
                "FE",
                100,
                200,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                FiscalEnvironment.PRODUCTION);

        NumberingResolutionResult result = service.create(command);

        assertThat(result.prefix()).isEqualTo("FE");
        assertThat(result.currentNumber()).isEqualTo(99);
        assertThat(result.active()).isTrue();
    }

    @Test
    void assignFiscalNumberRequiresActiveIssuerProfile() {
        AssignFiscalNumberService service = new AssignFiscalNumberService(
                new InMemoryIssuerProfileRepository(),
                new InMemoryNumberingResolutionRepository());
        AssignFiscalNumberCommand command = new AssignFiscalNumberCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                DOCUMENT_DATE,
                FiscalEnvironment.TEST);

        assertThatThrownBy(() -> service.assign(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("active issuer profile is required");
    }

    @Test
    void assignFiscalNumberUsesActiveResolutionAndAdvancesConsecutive() {
        InMemoryIssuerProfileRepository issuerRepository = new InMemoryIssuerProfileRepository();
        issuerRepository.save(IssuerProfile.configure(
                ISSUER_ID,
                COMPANY_ID,
                "ACME SAS",
                "900123456",
                "7",
                List.of(),
                "11001",
                "Calle 1 # 2-3"));
        InMemoryNumberingResolutionRepository resolutionRepository = new InMemoryNumberingResolutionRepository();
        resolutionRepository.save(NumberingResolution.create(
                RESOLUTION_ID,
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                "18760000003",
                "POS",
                50,
                51,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31),
                FiscalEnvironment.TEST));
        AssignFiscalNumberService service = new AssignFiscalNumberService(
                issuerRepository,
                resolutionRepository);
        AssignFiscalNumberCommand command = new AssignFiscalNumberCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                DOCUMENT_DATE,
                FiscalEnvironment.TEST);

        FiscalNumberResult first = service.assign(command);
        FiscalNumberResult second = service.assign(command);

        assertThat(first.resolutionId()).isEqualTo(RESOLUTION_ID);
        assertThat(first.prefix()).isEqualTo("POS");
        assertThat(first.number()).isEqualTo(50);
        assertThat(second.number()).isEqualTo(51);
        assertThat(resolutionRepository.savedResolution().currentNumber()).isEqualTo(51);
    }

    private static final class InMemoryIssuerProfileRepository implements IssuerProfileRepositoryPort {

        private final Map<UUID, IssuerProfile> issuerProfiles = new HashMap<>();

        @Override
        public IssuerProfile save(IssuerProfile issuerProfile) {
            issuerProfiles.put(issuerProfile.companyId(), issuerProfile);
            return issuerProfile;
        }

        @Override
        public Optional<IssuerProfile> findActiveByCompanyId(UUID companyId) {
            return Optional.ofNullable(issuerProfiles.get(companyId))
                    .filter(IssuerProfile::active);
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
        public Optional<NumberingResolution> findActiveResolution(
                UUID companyId,
                ElectronicDocumentType documentType,
                FiscalEnvironment environment,
                LocalDate documentDate) {
            return resolutions.values().stream()
                    .filter(resolution -> resolution.isAvailableFor(companyId, documentType, documentDate, environment))
                    .findFirst();
        }

        NumberingResolution savedResolution() {
            return savedResolution;
        }
    }
}

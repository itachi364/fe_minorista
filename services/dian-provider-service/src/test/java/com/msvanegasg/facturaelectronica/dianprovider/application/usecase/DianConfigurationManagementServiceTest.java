package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.CertificateMetadata;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianConfigurationRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalArtifactPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianEnvironment;

class DianConfigurationManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CONFIG_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-08-24T10:00:00Z");

    @Test
    void validatesTechnicalArtifactsBeforeTestingRealMode() {
        InMemoryConfigurationRepository repository = new InMemoryConfigurationRepository();
        DianConfigurationManagementService service = new DianConfigurationManagementService(repository,
                (companyId, name, value) -> companySecretRef(companyId, name), new StubCertificateExtractor(),
                () -> {
                    throw new DianConfigurationIncompleteException("No existe artefacto tecnico DIAN: UBL invoice XSD.");
                },
                () -> CONFIG_ID, () -> NOW);
        service.save(realCommand());

        assertThatThrownBy(() -> service.testConnection(COMPANY_ID, UUID.randomUUID()))
                .isInstanceOf(DianConfigurationIncompleteException.class)
                .hasMessageContaining("artefacto tecnico DIAN");
    }

    @Test
    void realModeConfigurationCanBeTestedWhenArtifactsAreAvailable() {
        InMemoryConfigurationRepository repository = new InMemoryConfigurationRepository();
        DianConfigurationManagementService service = new DianConfigurationManagementService(repository,
                (companyId, name, value) -> companySecretRef(companyId, name), new StubCertificateExtractor(),
                new AvailableArtifacts(), () -> CONFIG_ID, () -> NOW);
        service.save(realCommand());

        var result = service.testConnection(COMPANY_ID, UUID.randomUUID());

        assertThat(result.lastTestMessage()).contains("Configuracion real lista");
        assertThat(result.certificateAlias()).isEqualTo("certificado empresa");
        assertThat(result.certificateFingerprint()).isEqualTo("sha256:fingerprint");
        assertThat(result.certificateExpiresAt()).isEqualTo(NOW.plusSeconds(86_400));
    }

    @Test
    void rejectsExistingCertificateRefThatDoesNotBelongToCompany() {
        InMemoryConfigurationRepository repository = new InMemoryConfigurationRepository();
        repository.configuration = new DianCompanyConfiguration(CONFIG_ID, COMPANY_ID, DianConnectionMode.REAL,
                DianEnvironment.TEST, "software-id", companySecretRef(COMPANY_ID, "dian/software-pin"),
                companySecretRef(COMPANY_ID, "dian/technical-key"),
                companySecretRef(UUID.fromString("33333333-3333-3333-3333-333333333333"), "dian/certificate"),
                "certificado ajeno", "sha256:other", NOW.plusSeconds(86_400), "https://vpfe-hab.dian.gov.co",
                "test-set", true, null, null, null, null, UUID.randomUUID(), NOW, NOW);
        DianConfigurationManagementService service = new DianConfigurationManagementService(repository,
                (companyId, name, value) -> companySecretRef(companyId, name), new StubCertificateExtractor(),
                new AvailableArtifacts(), () -> CONFIG_ID, () -> NOW);

        assertThatThrownBy(() -> service.save(realCommandWithoutCertificateFile()))
                .isInstanceOf(DianConfigurationIncompleteException.class)
                .hasMessageContaining("no pertenece a la empresa");
    }

    private static DianConfigurationCommand realCommand() {
        return new DianConfigurationCommand(COMPANY_ID, DianConnectionMode.REAL, DianEnvironment.TEST, "software-id",
                "software-pin", "technical-key", "empresa.p12", new byte[] { 1, 2, 3 }, "password",
                "https://vpfe-hab.dian.gov.co", "test-set", true, UUID.randomUUID());
    }

    private static DianConfigurationCommand realCommandWithoutCertificateFile() {
        return new DianConfigurationCommand(COMPANY_ID, DianConnectionMode.REAL, DianEnvironment.TEST, "software-id",
                "software-pin", "technical-key", null, null, "password", "https://vpfe-hab.dian.gov.co",
                "test-set", true, UUID.randomUUID());
    }

    private static String companySecretRef(UUID companyId, String secretName) {
        return "/facturaelectronica/test/companies/" + companyId + "/" + secretName;
    }

    private static final class InMemoryConfigurationRepository implements DianConfigurationRepositoryPort {
        private DianCompanyConfiguration configuration;

        @Override
        public DianCompanyConfiguration save(DianCompanyConfiguration configuration) {
            this.configuration = configuration;
            return configuration;
        }

        @Override
        public Optional<DianCompanyConfiguration> findByCompanyId(UUID companyId) {
            return Optional.ofNullable(configuration).filter(value -> value.companyId().equals(companyId));
        }
    }

    private static final class AvailableArtifacts implements DianTechnicalArtifactPort {
        @Override
        public void ensureReadyForRealMode() {
        }
    }

    private static final class StubCertificateExtractor implements
            com.msvanegasg.facturaelectronica.dianprovider.application.port.out.CertificateMetadataExtractorPort {
        @Override
        public CertificateMetadata extract(String fileName, byte[] content, String password) {
            return new CertificateMetadata("certificado empresa", "sha256:fingerprint", NOW.plusSeconds(86_400));
        }
    }
}

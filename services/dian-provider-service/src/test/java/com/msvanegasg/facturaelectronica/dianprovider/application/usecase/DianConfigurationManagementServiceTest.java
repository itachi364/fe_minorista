package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

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
                (companyId, name, value) -> "secret://" + companyId + "/" + name,
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
                (companyId, name, value) -> "secret://" + companyId + "/" + name,
                new AvailableArtifacts(), () -> CONFIG_ID, () -> NOW);
        service.save(realCommand());

        var result = service.testConnection(COMPANY_ID, UUID.randomUUID());

        assertThat(result.lastTestMessage()).contains("Configuracion real lista");
    }

    private static DianConfigurationCommand realCommand() {
        return new DianConfigurationCommand(COMPANY_ID, DianConnectionMode.REAL, DianEnvironment.TEST, "software-id",
                "software-pin", "technical-key", "certificate", "password", "certificado empresa",
                "sha256:fingerprint", NOW.plusSeconds(86_400), "https://vpfe-hab.dian.gov.co", "test-set",
                true, UUID.randomUUID());
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
}

package com.msvanegasg.facturaelectronica.billing.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;
import com.msvanegasg.facturaelectronica.billing.domain.model.NumberingResolution;
import com.msvanegasg.facturaelectronica.billingservice.BillingServiceApplication;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = BillingServiceApplication.class)
@Import({ IssuerProfilePersistenceAdapter.class, NumberingResolutionPersistenceAdapter.class })
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class FiscalConfigurationPersistenceAdapterTest {

    @Autowired
    private IssuerProfilePersistenceAdapter issuerAdapter;

    @Autowired
    private NumberingResolutionPersistenceAdapter resolutionAdapter;

    @Test
    void persistsAndFindsActiveIssuerProfile() {
        UUID companyId = UUID.randomUUID();
        issuerAdapter.save(IssuerProfile.configure(UUID.randomUUID(), companyId, "ACME SAS", "900123456", "7",
                List.of("O-13"), "11001", "Calle 1 # 2-3"));

        IssuerProfile found = issuerAdapter.findActiveByCompanyId(companyId).orElseThrow();

        assertThat(found.legalName()).isEqualTo("ACME SAS");
        assertThat(found.taxResponsibilities()).containsExactly("O-13");
    }

    @Test
    void persistsAndFindsActiveResolutionByDate() {
        UUID companyId = UUID.randomUUID();
        resolutionAdapter.save(NumberingResolution.create(UUID.randomUUID(), companyId,
                ElectronicDocumentType.ELECTRONIC_POS, "18760000001", "POS", 100, 200,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), FiscalEnvironment.TEST));

        NumberingResolution found = resolutionAdapter.findActiveResolution(companyId,
                ElectronicDocumentType.ELECTRONIC_POS, FiscalEnvironment.TEST, LocalDate.of(2026, 5, 20))
                .orElseThrow();

        assertThat(found.prefix()).isEqualTo("POS");
        assertThat(found.currentNumber()).isEqualTo(99);
    }
}

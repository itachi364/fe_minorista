package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicense;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyLicenseJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyLicenseJpaRepository;

@ExtendWith(MockitoExtension.class)
class CompanyLicensePersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID LICENSE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    @Mock
    private CompanyLicenseJpaRepository repository;

    @Test
    void savesAndRestoresCompanyLicense() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CompanyLicensePersistenceAdapter adapter = new CompanyLicensePersistenceAdapter(repository);

        CompanyLicense saved = adapter.save(license());

        assertThat(saved.id()).isEqualTo(LICENSE_ID);
        assertThat(saved.companyId()).isEqualTo(COMPANY_ID);
        assertThat(saved.status()).isEqualTo(CompanyLicenseStatus.ACTIVE);
    }

    @Test
    void findsCompanyLicenseByCompanyId() {
        when(repository.findByCompanyId(COMPANY_ID)).thenReturn(Optional.of(entity()));
        CompanyLicensePersistenceAdapter adapter = new CompanyLicensePersistenceAdapter(repository);

        Optional<CompanyLicense> result = adapter.findByCompanyId(COMPANY_ID);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().planCode()).isEqualTo("SMALL_BUSINESS");
    }

    private static CompanyLicense license() {
        return new CompanyLicense(LICENSE_ID, COMPANY_ID, "SMALL_BUSINESS", CompanyLicenseStatus.ACTIVE,
                LocalDate.parse("2026-05-01"), LocalDate.parse("2027-05-01"), 5, 1000, NOW, NOW);
    }

    private static CompanyLicenseJpaEntity entity() {
        return new CompanyLicenseJpaEntity(LICENSE_ID, COMPANY_ID, "SMALL_BUSINESS", CompanyLicenseStatus.ACTIVE,
                LocalDate.parse("2026-05-01"), LocalDate.parse("2027-05-01"), 5, 1000, NOW, NOW);
    }
}

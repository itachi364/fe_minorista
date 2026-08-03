package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyStatus;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyJpaRepository;

@ExtendWith(MockitoExtension.class)
class CompanyPersistenceAdapterTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Integer IDENTIFICATION_TYPE_CODE = 31;
    private static final Instant NOW = Instant.parse("2026-05-19T10:00:00Z");

    @Mock
    private CompanyJpaRepository repository;

    @Test
    void savesAndRestoresCompany() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        CompanyPersistenceAdapter adapter = new CompanyPersistenceAdapter(repository);

        Company saved = adapter.save(company());

        assertThat(saved.id()).isEqualTo(COMPANY_ID);
        assertThat(saved.status()).isEqualTo(CompanyStatus.ACTIVE);
        assertThat(saved.identificationNumber()).isEqualTo("900123456");
    }

    @Test
    void findsCompanyById() {
        when(repository.findById(COMPANY_ID)).thenReturn(Optional.of(entity()));
        CompanyPersistenceAdapter adapter = new CompanyPersistenceAdapter(repository);

        Optional<Company> result = adapter.findById(COMPANY_ID);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().legalName()).isEqualTo("Mi Empresa SAS");
    }

    @Test
    void checksDuplicatedIdentification() {
        when(repository.existsByIdentificationTypeCodeAndIdentificationNumber(IDENTIFICATION_TYPE_CODE, "900123456"))
                .thenReturn(true);
        CompanyPersistenceAdapter adapter = new CompanyPersistenceAdapter(repository);

        boolean exists = adapter.existsByIdentification(IDENTIFICATION_TYPE_CODE, "900123456");

        assertThat(exists).isTrue();
    }

    private static Company company() {
        return new Company(COMPANY_ID, "Mi Empresa SAS", "Mi Tienda", IDENTIFICATION_TYPE_CODE, "900123456", "7",
                "admin@example.com", CompanyStatus.ACTIVE, NOW, NOW);
    }

    private static CompanyJpaEntity entity() {
        return new CompanyJpaEntity(COMPANY_ID, "Mi Empresa SAS", "Mi Tienda", IDENTIFICATION_TYPE_CODE, "900123456",
                "7", "admin@example.com", CompanyStatus.ACTIVE, NOW, NOW);
    }
}

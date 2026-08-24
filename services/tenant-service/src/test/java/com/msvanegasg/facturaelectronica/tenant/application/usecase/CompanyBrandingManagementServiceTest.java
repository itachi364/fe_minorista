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

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.BrandingAssetStoragePort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyBrandingRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.BrandingAssetPurpose;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyBranding;

class CompanyBrandingManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-08-24T10:00:00Z");

    private final InMemoryCompanyRepository companyRepository = new InMemoryCompanyRepository();
    private final InMemoryBrandingRepository brandingRepository = new InMemoryBrandingRepository();
    private final InMemoryAssetStorage storage = new InMemoryAssetStorage();
    private final CompanyBrandingManagementService service = new CompanyBrandingManagementService(
            companyRepository,
            brandingRepository,
            storage,
            fixedClock());

    @Test
    void updatesCompanyBrandingMetadata() {
        CompanyBrandingResult result = service.update(COMPANY_ID,
                new CompanyBrandingCommand("Necto Cafe", "#1f78a8", "#2a7c61", USER_ID));

        assertThat(result.displayName()).isEqualTo("Necto Cafe");
        assertThat(result.primaryColor()).isEqualTo("#1f78a8");
        assertThat(result.accentColor()).isEqualTo("#2a7c61");
        assertThat(result.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void uploadsAndReadsAllowedBrandingAsset() {
        byte[] content = "png-content".getBytes();

        CompanyBrandingResult result = service.uploadAsset(COMPANY_ID,
                new CompanyBrandingAssetCommand(BrandingAssetPurpose.HEADER_LOGO, "logo.jpeg", "image/jpeg",
                        content, USER_ID));
        CompanyBrandingAssetResult asset = service.readAsset(COMPANY_ID, BrandingAssetPurpose.HEADER_LOGO);

        assertThat(result.headerLogoUrl()).contains("/branding/assets/HEADER_LOGO?hash=");
        assertThat(asset.content()).isEqualTo(content);
        assertThat(asset.contentType()).isEqualTo("image/jpeg");
        assertThat(asset.contentHash()).hasSize(64);
        assertThat(storage.savedKeys()).singleElement().asString().endsWith(".jpg");
    }

    @Test
    void rejectsInvalidColorAndUnsafeAssetType() {
        assertThatThrownBy(() -> service.update(COMPANY_ID,
                new CompanyBrandingCommand("Necto Cafe", "blue", "#2a7c61", USER_ID)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.uploadAsset(COMPANY_ID,
                new CompanyBrandingAssetCommand(BrandingAssetPurpose.FAVICON, "icon.svg", "image/svg+xml",
                        "<svg />".getBytes(), USER_ID)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static ClockPort fixedClock() {
        return () -> NOW;
    }

    private static final class InMemoryCompanyRepository implements CompanyRepositoryPort {

        private final Company company = Company.create(COMPANY_ID, "Necto SAS", "Necto", 31, "900123456", "7",
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

    private static final class InMemoryBrandingRepository implements CompanyBrandingRepositoryPort {

        private final Map<UUID, CompanyBranding> brandings = new HashMap<>();

        @Override
        public CompanyBranding save(CompanyBranding branding) {
            brandings.put(branding.companyId(), branding);
            return branding;
        }

        @Override
        public Optional<CompanyBranding> findByCompanyId(UUID companyId) {
            return Optional.ofNullable(brandings.get(companyId));
        }
    }

    private static final class InMemoryAssetStorage implements BrandingAssetStoragePort {

        private final Map<String, byte[]> assets = new HashMap<>();

        @Override
        public void save(String storageKey, byte[] content) {
            assets.put(storageKey, content);
        }

        @Override
        public byte[] read(String storageKey) {
            return assets.get(storageKey);
        }

        private List<String> savedKeys() {
            return List.copyOf(assets.keySet());
        }
    }
}

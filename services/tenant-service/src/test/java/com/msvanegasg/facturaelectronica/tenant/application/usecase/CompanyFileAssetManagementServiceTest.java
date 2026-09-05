package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileAssetRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyFileStoragePort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.Company;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileAsset;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileCategory;

class CompanyFileAssetManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID ASSET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-09-03T10:00:00Z");

    private final InMemoryCompanyRepository companies = new InMemoryCompanyRepository();
    private final InMemoryFileAssetRepository assets = new InMemoryFileAssetRepository();
    private final InMemoryStorage storage = new InMemoryStorage();
    private final CompanyFileAssetManagementService service = new CompanyFileAssetManagementService(
            companies,
            assets,
            storage,
            () -> ASSET_ID,
            () -> NOW,
            Duration.ofSeconds(60),
            "unit-test-download-secret");

    @Test
    void storesPdfEvidenceUnderCompanyCategoryPrefix() {
        companies.save(company());

        CompanyFileAssetResult result = service.upload(COMPANY_ID, new CompanyFileAssetCommand(
                CompanyFileCategory.PURCHASE_EVIDENCE,
                "factura proveedor.pdf",
                "application/pdf",
                "%PDF-1.7 content".getBytes(StandardCharsets.UTF_8),
                USER_ID));

        assertThat(result.url()).contains("/api/v1/companies/" + COMPANY_ID + "/files/" + ASSET_ID);
        assertThat(storage.objects).containsKey(COMPANY_ID + "/facturas/" + ASSET_ID + "-factura_proveedor.pdf");
    }

    @Test
    void createsSignedTemporaryDownloadLinkForLocalStorage() {
        companies.save(company());
        service.upload(COMPANY_ID, new CompanyFileAssetCommand(
                CompanyFileCategory.PURCHASE_EVIDENCE,
                "factura.pdf",
                "application/pdf",
                "%PDF-1.7 content".getBytes(StandardCharsets.UTF_8),
                USER_ID));

        var link = service.createDownloadLink(COMPANY_ID, ASSET_ID);

        assertThat(link.ttlSeconds()).isEqualTo(60);
        assertThat(link.expiresAt()).isEqualTo(NOW.plusSeconds(60));
        assertThat(link.url()).contains("/api/v1/companies/" + COMPANY_ID + "/files/" + ASSET_ID + "/download");
        assertThat(link.url()).contains("signature=");
    }

    @Test
    void readsSignedDownloadBeforeExpirationAndRejectsExpiredLink() {
        companies.save(company());
        byte[] content = "%PDF-1.7 content".getBytes(StandardCharsets.UTF_8);
        service.upload(COMPANY_ID, new CompanyFileAssetCommand(
                CompanyFileCategory.EXPENSE_EVIDENCE,
                "gasto.pdf",
                "application/pdf",
                content,
                USER_ID));
        var link = service.createDownloadLink(COMPANY_ID, ASSET_ID);
        long expiresAt = Long.parseLong(queryValue(link.url(), "expiresAt"));
        String signature = queryValue(link.url(), "signature");

        assertThat(service.readSigned(COMPANY_ID, ASSET_ID, expiresAt, signature).content()).isEqualTo(content);
        assertThatThrownBy(() -> service.readSigned(COMPANY_ID, ASSET_ID, NOW.minusSeconds(1).getEpochSecond(),
                signature))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("download link expired");
    }

    @Test
    void rejectsPdfEvidenceWithoutPdfSignature() {
        companies.save(company());

        assertThatThrownBy(() -> service.upload(COMPANY_ID, new CompanyFileAssetCommand(
                CompanyFileCategory.EXPENSE_EVIDENCE,
                "gasto.pdf",
                "application/pdf",
                "not a pdf".getBytes(StandardCharsets.UTF_8),
                USER_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo se permite PDF");
    }

    @Test
    void rejectsKnownUnsafeFileSignature() {
        companies.save(company());

        assertThatThrownBy(() -> service.upload(COMPANY_ID, new CompanyFileAssetCommand(
                CompanyFileCategory.LOGO,
                "logo.txt",
                "text/plain",
                "X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*"
                        .getBytes(StandardCharsets.ISO_8859_1),
                USER_ID)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El archivo no supero la validacion de seguridad.");
    }

    private static String queryValue(String url, String name) {
        String query = url.substring(url.indexOf('?') + 1);
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts[0].equals(name)) {
                return parts[1];
            }
        }
        throw new IllegalArgumentException("query parameter not found: " + name);
    }

    private static Company company() {
        return Company.create(COMPANY_ID, "Empresa SAS", "Empresa", 31, "900123456", "7", "admin@example.com", NOW);
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
            return false;
        }
    }

    private static class InMemoryFileAssetRepository implements CompanyFileAssetRepositoryPort {

        private final Map<UUID, CompanyFileAsset> assets = new HashMap<>();

        @Override
        public CompanyFileAsset save(CompanyFileAsset asset) {
            assets.put(asset.id(), asset);
            return asset;
        }

        @Override
        public Optional<CompanyFileAsset> findByCompanyIdAndId(UUID companyId, UUID id) {
            return Optional.ofNullable(assets.get(id)).filter(asset -> asset.companyId().equals(companyId));
        }
    }

    private static class InMemoryStorage implements CompanyFileStoragePort {

        private final Map<String, byte[]> objects = new HashMap<>();

        @Override
        public void save(String storageKey, String contentType, byte[] content) {
            objects.put(storageKey, content);
        }

        @Override
        public byte[] read(String storageKey) {
            return objects.get(storageKey);
        }
    }
}

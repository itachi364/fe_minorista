package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.VersionedCatalogRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.application.dto.AuditContext;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogAuditEventCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogDefinition;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogItem;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Department;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Municipality;

class VersionedCatalogManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    @Test
    void returnsCompanyOverlayForCatalogItems() {
        InMemoryVersionedCatalogRepository repository = new InMemoryVersionedCatalogRepository();
        repository.setting.put("PAYMENT_METHOD:CASH", false);
        VersionedCatalogManagementService service = new VersionedCatalogManagementService(repository);

        var items = service.findCompanyItems(COMPANY_ID, "payment_method");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).code()).isEqualTo("CASH");
        assertThat(items.get(0).enabledForCompany()).isFalse();
    }

    @Test
    void listsDefinitionsAndCreatesGlobalCatalogItems() {
        InMemoryVersionedCatalogRepository repository = new InMemoryVersionedCatalogRepository();
        VersionedCatalogManagementService service = new VersionedCatalogManagementService(repository);

        var created = service.createGlobalItem("payment_method",
                new CatalogItemCommand("qr_code", "Codigo QR", "Pago por QR", false, "APP", "2026-08",
                        null, null, 30));

        assertThat(service.findDefinitions()).extracting("code").contains("PAYMENT_METHOD");
        assertThat(created.catalogCode()).isEqualTo("PAYMENT_METHOD");
        assertThat(created.code()).isEqualTo("QR_CODE");
        assertThat(created.label()).isEqualTo("Codigo QR");
        assertThat(service.findGlobalItems("PAYMENT_METHOD", true)).extracting("code").contains("CASH", "QR_CODE");
    }

    @Test
    void updatesAndDeactivatesGlobalCatalogItems() {
        InMemoryVersionedCatalogRepository repository = new InMemoryVersionedCatalogRepository();
        VersionedCatalogManagementService service = new VersionedCatalogManagementService(repository);

        var updated = service.updateGlobalItem("PAYMENT_METHOD", "CASH",
                new CatalogItemCommand("IGNORED", "Efectivo en caja", "Pago contado", false, "APP",
                        "2026-09", null, null, 5));
        var inactive = service.setGlobalItemActive("PAYMENT_METHOD", "CASH", false);

        assertThat(updated.code()).isEqualTo("CASH");
        assertThat(updated.label()).isEqualTo("Efectivo en caja");
        assertThat(inactive.active()).isFalse();
        assertThat(service.findGlobalItems("PAYMENT_METHOD")).isEmpty();
        assertThat(service.findGlobalItems("PAYMENT_METHOD", true)).hasSize(1);
    }

    @Test
    void auditsGlobalCatalogMutations() {
        InMemoryVersionedCatalogRepository repository = new InMemoryVersionedCatalogRepository();
        List<CatalogAuditEventCommand> events = new java.util.ArrayList<>();
        VersionedCatalogManagementService service = new VersionedCatalogManagementService(repository, events::add);
        UUID userId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        AuditContext context = new AuditContext(COMPANY_ID, userId, "correlation-1");

        service.updateGlobalItem("PAYMENT_METHOD", "CASH",
                new CatalogItemCommand("IGNORED", "Efectivo en caja", "Pago contado", false, "APP",
                        "2026-09", null, null, 5), context);
        service.setGlobalItemActive("PAYMENT_METHOD", "CASH", false, context);

        assertThat(events).extracting(CatalogAuditEventCommand::action)
                .containsExactly("UPDATE_CATALOG_ITEM", "SET_CATALOG_ITEM_ACTIVE");
        assertThat(events).allSatisfy(event -> {
            assertThat(event.companyId()).isEqualTo(COMPANY_ID);
            assertThat(event.userId()).isEqualTo(userId);
            assertThat(event.resourceType()).isEqualTo("CATALOG_ITEM");
            assertThat(event.result()).isEqualTo("SUCCESS");
            assertThat(event.detail()).contains("correlation-1");
        });
    }

    @Test
    void auditsCompanyCatalogActivation() {
        InMemoryVersionedCatalogRepository repository = new InMemoryVersionedCatalogRepository();
        List<CatalogAuditEventCommand> events = new java.util.ArrayList<>();
        VersionedCatalogManagementService service = new VersionedCatalogManagementService(repository, events::add);

        service.setCompanyItemEnabled(COMPANY_ID, "PAYMENT_METHOD", "CASH", false,
                new AuditContext(COMPANY_ID, null, "correlation-2"));

        assertThat(events).singleElement().satisfies(event -> {
            assertThat(event.action()).isEqualTo("SET_COMPANY_CATALOG_ITEM_ENABLED");
            assertThat(event.result()).isEqualTo("SUCCESS");
            assertThat(event.detail()).contains("enabled=false");
        });
    }

    @Test
    void rejectsGlobalEditWhenDefinitionDoesNotAllowIt() {
        InMemoryVersionedCatalogRepository repository = new InMemoryVersionedCatalogRepository();
        VersionedCatalogManagementService service = new VersionedCatalogManagementService(repository);

        assertThatThrownBy(() -> service.createGlobalItem("DIAN_DOCUMENT_TYPE",
                new CatalogItemCommand("99", "Documento nuevo", null, true, "DIAN", "2026-08", null, null, 99)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be edited");
    }

    @Test
    void doesNotEnableInactiveGlobalItemForCompany() {
        InMemoryVersionedCatalogRepository repository = new InMemoryVersionedCatalogRepository();
        repository.items.put("PAYMENT_METHOD:OLD", CatalogItem.restore("PAYMENT_METHOD", "OLD", "Old", null,
                false, false, "APP", "2026-08", 20));
        VersionedCatalogManagementService service = new VersionedCatalogManagementService(repository);

        assertThatThrownBy(() -> service.setCompanyItemEnabled(COMPANY_ID, "PAYMENT_METHOD", "OLD", true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive");
    }

    @Test
    void returnsMunicipalitiesByDepartment() {
        VersionedCatalogManagementService service =
                new VersionedCatalogManagementService(new InMemoryVersionedCatalogRepository());

        var municipalities = service.findMunicipalitiesByDepartment("11");

        assertThat(municipalities).extracting("code").containsExactly("11001");
    }

    private static final class InMemoryVersionedCatalogRepository implements VersionedCatalogRepositoryPort {

        private final Map<String, CatalogItem> items = new HashMap<>();
        private final Map<String, Boolean> setting = new HashMap<>();
        private final Map<String, CatalogDefinition> definitions = new HashMap<>();

        private InMemoryVersionedCatalogRepository() {
            definitions.put("PAYMENT_METHOD", CatalogDefinition.restore("PAYMENT_METHOD", "Metodos de pago",
                    "Opciones de pago", false, true, true, true, 10));
            definitions.put("DIAN_DOCUMENT_TYPE", CatalogDefinition.restore("DIAN_DOCUMENT_TYPE",
                    "Tipos de documento DIAN", "Tipos oficiales", true, false, false, true, 20));
            items.put("PAYMENT_METHOD:CASH", CatalogItem.restore("PAYMENT_METHOD", "CASH", "Efectivo", null,
                    true, false, "APP", "2026-08", 10));
        }

        @Override
        public List<CatalogDefinition> findActiveCatalogDefinitions() {
            return definitions.values().stream().filter(CatalogDefinition::active).toList();
        }

        @Override
        public Optional<CatalogDefinition> findCatalogDefinition(String catalogCode) {
            return Optional.ofNullable(definitions.get(catalogCode));
        }

        @Override
        public List<CatalogItem> findActiveCatalogItems(String catalogCode) {
            return items.values().stream()
                    .filter(item -> item.catalogCode().equals(catalogCode))
                    .filter(CatalogItem::active)
                    .toList();
        }

        @Override
        public List<CatalogItem> findCatalogItems(String catalogCode, boolean includeInactive) {
            return items.values().stream()
                    .filter(item -> item.catalogCode().equals(catalogCode))
                    .filter(item -> includeInactive || item.active())
                    .toList();
        }

        @Override
        public Optional<CatalogItem> findCatalogItem(String catalogCode, String itemCode) {
            return Optional.ofNullable(items.get(catalogCode + ":" + itemCode));
        }

        @Override
        public CatalogItem saveCatalogItem(CatalogItem item) {
            items.put(item.catalogCode() + ":" + item.itemCode(), item);
            return item;
        }

        @Override
        public Optional<Boolean> findCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode) {
            return Optional.ofNullable(setting.get(catalogCode + ":" + itemCode));
        }

        @Override
        public void saveCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode, boolean enabled) {
            setting.put(catalogCode + ":" + itemCode, enabled);
        }

        @Override
        public List<Department> findActiveDepartments() {
            return List.of(Department.restore("11", "Bogota, D.C.", true, "DANE DIVIPOLA", "2025", 10));
        }

        @Override
        public List<Municipality> findActiveMunicipalitiesByDepartment(String departmentCode) {
            return List.of(Municipality.restore("11001", departmentCode, "Bogota, D.C.", true,
                    "DANE DIVIPOLA", "2025", 10));
        }

        @Override
        public Optional<Municipality> findMunicipality(String municipalityCode) {
            return Optional.of(Municipality.restore(municipalityCode, "11", "Bogota, D.C.", true,
                    "DANE DIVIPOLA", "2025", 10));
        }
    }
}

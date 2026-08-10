package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogDefinitionResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.AuditContext;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogAuditEventCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.DepartmentResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.MunicipalityResult;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageVersionedCatalogUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CatalogAuditEventPort;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.VersionedCatalogRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogDefinition;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogItem;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Department;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Municipality;

public class VersionedCatalogManagementService implements ManageVersionedCatalogUseCase {

    private final VersionedCatalogRepositoryPort repository;
    private final CatalogAuditEventPort auditEventPort;

    public VersionedCatalogManagementService(VersionedCatalogRepositoryPort repository) {
        this(repository, CatalogAuditEventPort.noop());
    }

    public VersionedCatalogManagementService(VersionedCatalogRepositoryPort repository,
            CatalogAuditEventPort auditEventPort) {
        this.repository = Objects.requireNonNull(repository);
        this.auditEventPort = Objects.requireNonNull(auditEventPort);
    }

    @Override
    public List<CatalogDefinitionResult> findDefinitions() {
        return repository.findActiveCatalogDefinitions().stream()
                .map(VersionedCatalogManagementService::toResult)
                .toList();
    }

    @Override
    public List<CatalogItemResult> findGlobalItems(String catalogCode) {
        return findGlobalItems(catalogCode, false);
    }

    @Override
    public List<CatalogItemResult> findGlobalItems(String catalogCode, boolean includeInactive) {
        return repository.findCatalogItems(normalizeCode(catalogCode), includeInactive).stream()
                .map(item -> toResult(item, true))
                .toList();
    }

    @Override
    public CatalogItemResult createGlobalItem(String catalogCode, CatalogItemCommand command) {
        return createGlobalItem(catalogCode, command, AuditContext.empty());
    }

    @Override
    public CatalogItemResult createGlobalItem(String catalogCode, CatalogItemCommand command,
            AuditContext auditContext) {
        String normalizedCatalogCode = normalizeCode(catalogCode);
        String normalizedItemCode = command == null ? null : command.code();
        try {
            normalizedItemCode = normalizeCode(normalizedItemCode);
            CatalogDefinition definition = requireDefinition(normalizedCatalogCode);
            if (!definition.globalEditableByRoot()) {
                throw new IllegalArgumentException("catalog cannot be edited globally from UI");
            }
            if (repository.findCatalogItem(normalizedCatalogCode, normalizedItemCode).isPresent()) {
                throw new IllegalArgumentException("catalog item already exists");
            }
            CatalogItem item = toCatalogItem(normalizedCatalogCode, normalizedItemCode, command, true);
            CatalogItemResult result = toResult(repository.saveCatalogItem(item), true);
            audit(auditContext, "CREATE_CATALOG_ITEM", normalizedCatalogCode, normalizedItemCode, "SUCCESS", null);
            return result;
        } catch (RuntimeException ex) {
            audit(auditContext, "CREATE_CATALOG_ITEM", normalizedCatalogCode, normalizedItemCode, "FAILURE",
                    ex.getMessage());
            throw ex;
        }
    }

    @Override
    public CatalogItemResult updateGlobalItem(String catalogCode, String itemCode, CatalogItemCommand command) {
        return updateGlobalItem(catalogCode, itemCode, command, AuditContext.empty());
    }

    @Override
    public CatalogItemResult updateGlobalItem(String catalogCode, String itemCode, CatalogItemCommand command,
            AuditContext auditContext) {
        String normalizedCatalogCode = normalizeCode(catalogCode);
        String normalizedItemCode = normalizeCode(itemCode);
        try {
            CatalogDefinition definition = requireDefinition(normalizedCatalogCode);
            if (!definition.globalEditableByRoot()) {
                throw new IllegalArgumentException("catalog cannot be edited globally from UI");
            }
            CatalogItem current = repository.findCatalogItem(normalizedCatalogCode, normalizedItemCode)
                    .orElseThrow(() -> new IllegalArgumentException("catalog item was not found"));
            CatalogItem item = toCatalogItem(normalizedCatalogCode, current.itemCode(), command, current.active());
            CatalogItemResult result = toResult(repository.saveCatalogItem(item), true);
            audit(auditContext, "UPDATE_CATALOG_ITEM", normalizedCatalogCode, normalizedItemCode, "SUCCESS", null);
            return result;
        } catch (RuntimeException ex) {
            audit(auditContext, "UPDATE_CATALOG_ITEM", normalizedCatalogCode, normalizedItemCode, "FAILURE",
                    ex.getMessage());
            throw ex;
        }
    }

    @Override
    public CatalogItemResult setGlobalItemActive(String catalogCode, String itemCode, boolean active) {
        return setGlobalItemActive(catalogCode, itemCode, active, AuditContext.empty());
    }

    @Override
    public CatalogItemResult setGlobalItemActive(String catalogCode, String itemCode, boolean active,
            AuditContext auditContext) {
        String normalizedCatalogCode = normalizeCode(catalogCode);
        String normalizedItemCode = normalizeCode(itemCode);
        try {
            CatalogDefinition definition = requireDefinition(normalizedCatalogCode);
            if (!definition.globalEditableByRoot()) {
                throw new IllegalArgumentException("catalog cannot be activated globally from UI");
            }
            CatalogItem current = repository.findCatalogItem(normalizedCatalogCode, normalizedItemCode)
                    .orElseThrow(() -> new IllegalArgumentException("catalog item was not found"));
            CatalogItem item = CatalogItem.restore(current.catalogCode(), current.itemCode(), current.label(),
                    current.description(), active, current.regulatory(), current.source(), current.sourceVersion(),
                    current.validFrom(), current.validTo(), current.sortOrder());
            CatalogItemResult result = toResult(repository.saveCatalogItem(item), true);
            audit(auditContext, "SET_CATALOG_ITEM_ACTIVE", normalizedCatalogCode, normalizedItemCode, "SUCCESS",
                    "active=" + active);
            return result;
        } catch (RuntimeException ex) {
            audit(auditContext, "SET_CATALOG_ITEM_ACTIVE", normalizedCatalogCode, normalizedItemCode, "FAILURE",
                    ex.getMessage());
            throw ex;
        }
    }

    @Override
    public List<CatalogItemResult> findCompanyItems(UUID companyId, String catalogCode) {
        UUID requiredCompanyId = Objects.requireNonNull(companyId, "companyId is required");
        String normalizedCatalogCode = normalizeCode(catalogCode);
        return repository.findActiveCatalogItems(normalizedCatalogCode).stream()
                .map(item -> toResult(item, repository.findCompanyItemEnabled(requiredCompanyId,
                        item.catalogCode(), item.itemCode()).orElse(true)))
                .toList();
    }

    @Override
    public CatalogItemResult setCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode,
            boolean enabled) {
        return setCompanyItemEnabled(companyId, catalogCode, itemCode, enabled, AuditContext.empty());
    }

    @Override
    public CatalogItemResult setCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode,
            boolean enabled, AuditContext auditContext) {
        UUID requiredCompanyId = Objects.requireNonNull(companyId, "companyId is required");
        String normalizedCatalogCode = normalizeCode(catalogCode);
        String normalizedItemCode = normalizeCode(itemCode);
        try {
            CatalogItem item = repository.findCatalogItem(normalizedCatalogCode, normalizedItemCode)
                    .orElseThrow(() -> new IllegalArgumentException("catalog item was not found"));
            if (enabled && !item.active()) {
                throw new IllegalArgumentException("inactive global catalog item cannot be enabled for company");
            }
            repository.saveCompanyItemEnabled(requiredCompanyId, item.catalogCode(), item.itemCode(), enabled);
            audit(auditContext, "SET_COMPANY_CATALOG_ITEM_ENABLED", normalizedCatalogCode, normalizedItemCode,
                    "SUCCESS", "enabled=" + enabled);
            return toResult(item, enabled);
        } catch (RuntimeException ex) {
            audit(auditContext, "SET_COMPANY_CATALOG_ITEM_ENABLED", normalizedCatalogCode, normalizedItemCode,
                    "FAILURE", ex.getMessage());
            throw ex;
        }
    }

    @Override
    public List<DepartmentResult> findDepartments() {
        return repository.findActiveDepartments().stream()
                .map(VersionedCatalogManagementService::toResult)
                .toList();
    }

    @Override
    public List<MunicipalityResult> findMunicipalitiesByDepartment(String departmentCode) {
        return repository.findActiveMunicipalitiesByDepartment(normalizeCode(departmentCode)).stream()
                .map(VersionedCatalogManagementService::toResult)
                .toList();
    }

    @Override
    public MunicipalityResult findMunicipality(String municipalityCode) {
        return repository.findMunicipality(normalizeCode(municipalityCode))
                .map(VersionedCatalogManagementService::toResult)
                .orElseThrow(() -> new IllegalArgumentException("municipality was not found"));
    }

    private static CatalogItemResult toResult(CatalogItem item, boolean enabledForCompany) {
        return new CatalogItemResult(item.catalogCode(), item.itemCode(), item.label(), item.description(),
                item.active(), enabledForCompany, item.regulatory(), item.source(), item.sourceVersion(),
                item.validFrom(), item.validTo(), item.sortOrder());
    }

    private static CatalogDefinitionResult toResult(CatalogDefinition definition) {
        return new CatalogDefinitionResult(definition.code(), definition.label(), definition.description(),
                definition.regulatory(), definition.companyConfigurable(), definition.globalEditableByRoot(),
                definition.active(), definition.sortOrder());
    }

    private static DepartmentResult toResult(Department department) {
        return new DepartmentResult(department.code(), department.name(), department.active(), department.source(),
                department.sourceVersion());
    }

    private static MunicipalityResult toResult(Municipality municipality) {
        return new MunicipalityResult(municipality.code(), municipality.departmentCode(), municipality.name(),
                municipality.active(), municipality.source(), municipality.sourceVersion());
    }

    private static String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("code is required");
        }
        return value.trim().toUpperCase();
    }

    private CatalogDefinition requireDefinition(String catalogCode) {
        return repository.findCatalogDefinition(catalogCode)
                .filter(CatalogDefinition::active)
                .orElseThrow(() -> new IllegalArgumentException("catalog was not found"));
    }

    private static CatalogItem toCatalogItem(String catalogCode, String itemCode, CatalogItemCommand command,
            boolean active) {
        Objects.requireNonNull(command, "command is required");
        return CatalogItem.restore(catalogCode, itemCode, command.label(), command.description(), active,
                command.regulatory(), command.source(), command.sourceVersion(), command.validFrom(),
                command.validTo(), command.sortOrder());
    }

    private void audit(AuditContext context, String action, String catalogCode, String itemCode, String result,
            String detail) {
        AuditContext safeContext = context == null ? AuditContext.empty() : context;
        String safeDetail = "{\"catalogCode\":\"%s\",\"itemCode\":\"%s\",\"correlationId\":\"%s\",\"detail\":\"%s\"}"
                .formatted(escape(catalogCode), escape(itemCode), escape(safeContext.correlationId()),
                        escape(detail));
        auditEventPort.register(new CatalogAuditEventCommand(safeContext.companyId(), safeContext.userId(),
                "CATALOG_ADMINISTRATION", "CATALOG_ITEM", truncate(catalogCode + "/" + itemCode, 120), action,
                result, safeDetail));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}

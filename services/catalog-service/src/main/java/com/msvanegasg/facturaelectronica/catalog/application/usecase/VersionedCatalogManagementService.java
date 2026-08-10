package com.msvanegasg.facturaelectronica.catalog.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogDefinitionResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.DepartmentResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.MunicipalityResult;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageVersionedCatalogUseCase;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.VersionedCatalogRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogDefinition;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogItem;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Department;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Municipality;

public class VersionedCatalogManagementService implements ManageVersionedCatalogUseCase {

    private final VersionedCatalogRepositoryPort repository;

    public VersionedCatalogManagementService(VersionedCatalogRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
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
        String normalizedCatalogCode = normalizeCode(catalogCode);
        CatalogDefinition definition = requireDefinition(normalizedCatalogCode);
        if (!definition.globalEditableByRoot()) {
            throw new IllegalArgumentException("catalog cannot be edited globally from UI");
        }
        String normalizedItemCode = normalizeCode(command.code());
        if (repository.findCatalogItem(normalizedCatalogCode, normalizedItemCode).isPresent()) {
            throw new IllegalArgumentException("catalog item already exists");
        }
        CatalogItem item = toCatalogItem(normalizedCatalogCode, normalizedItemCode, command, true);
        return toResult(repository.saveCatalogItem(item), true);
    }

    @Override
    public CatalogItemResult updateGlobalItem(String catalogCode, String itemCode, CatalogItemCommand command) {
        String normalizedCatalogCode = normalizeCode(catalogCode);
        CatalogDefinition definition = requireDefinition(normalizedCatalogCode);
        if (!definition.globalEditableByRoot()) {
            throw new IllegalArgumentException("catalog cannot be edited globally from UI");
        }
        CatalogItem current = repository.findCatalogItem(normalizedCatalogCode, normalizeCode(itemCode))
                .orElseThrow(() -> new IllegalArgumentException("catalog item was not found"));
        CatalogItem item = toCatalogItem(normalizedCatalogCode, current.itemCode(), command, current.active());
        return toResult(repository.saveCatalogItem(item), true);
    }

    @Override
    public CatalogItemResult setGlobalItemActive(String catalogCode, String itemCode, boolean active) {
        String normalizedCatalogCode = normalizeCode(catalogCode);
        CatalogDefinition definition = requireDefinition(normalizedCatalogCode);
        if (!definition.globalEditableByRoot()) {
            throw new IllegalArgumentException("catalog cannot be activated globally from UI");
        }
        CatalogItem current = repository.findCatalogItem(normalizedCatalogCode, normalizeCode(itemCode))
                .orElseThrow(() -> new IllegalArgumentException("catalog item was not found"));
        CatalogItem item = CatalogItem.restore(current.catalogCode(), current.itemCode(), current.label(),
                current.description(), active, current.regulatory(), current.source(), current.sourceVersion(),
                current.validFrom(), current.validTo(), current.sortOrder());
        return toResult(repository.saveCatalogItem(item), true);
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
        UUID requiredCompanyId = Objects.requireNonNull(companyId, "companyId is required");
        CatalogItem item = repository.findCatalogItem(normalizeCode(catalogCode), normalizeCode(itemCode))
                .orElseThrow(() -> new IllegalArgumentException("catalog item was not found"));
        if (enabled && !item.active()) {
            throw new IllegalArgumentException("inactive global catalog item cannot be enabled for company");
        }
        repository.saveCompanyItemEnabled(requiredCompanyId, item.catalogCode(), item.itemCode(), enabled);
        return toResult(item, enabled);
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
}

package com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.application.port.out.VersionedCatalogRepositoryPort;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogDefinition;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogItem;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Department;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Municipality;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CatalogDefinitionJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CatalogItemJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CatalogItemJpaId;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CompanyCatalogItemSettingJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.CompanyCatalogItemSettingJpaId;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.MunicipalityJpaEntity;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.CatalogDefinitionJpaRepository;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.CatalogItemJpaRepository;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.CompanyCatalogItemSettingJpaRepository;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.DepartmentJpaRepository;
import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.repository.MunicipalityJpaRepository;

@Component
public class VersionedCatalogPersistenceAdapter implements VersionedCatalogRepositoryPort {

    private final CatalogDefinitionJpaRepository catalogDefinitionRepository;
    private final CatalogItemJpaRepository catalogItemRepository;
    private final CompanyCatalogItemSettingJpaRepository companySettingRepository;
    private final DepartmentJpaRepository departmentRepository;
    private final MunicipalityJpaRepository municipalityRepository;

    public VersionedCatalogPersistenceAdapter(CatalogDefinitionJpaRepository catalogDefinitionRepository,
            CatalogItemJpaRepository catalogItemRepository,
            CompanyCatalogItemSettingJpaRepository companySettingRepository,
            DepartmentJpaRepository departmentRepository,
            MunicipalityJpaRepository municipalityRepository) {
        this.catalogDefinitionRepository = catalogDefinitionRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.companySettingRepository = companySettingRepository;
        this.departmentRepository = departmentRepository;
        this.municipalityRepository = municipalityRepository;
    }

    @Override
    public List<CatalogDefinition> findActiveCatalogDefinitions() {
        return catalogDefinitionRepository.findByActiveTrueOrderBySortOrderAscLabelAsc().stream()
                .map(VersionedCatalogPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<CatalogDefinition> findCatalogDefinition(String catalogCode) {
        return catalogDefinitionRepository.findById(catalogCode).map(VersionedCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public List<CatalogItem> findActiveCatalogItems(String catalogCode) {
        return catalogItemRepository.findByIdCatalogCodeAndActiveTrueOrderBySortOrderAscLabelAsc(catalogCode).stream()
                .map(VersionedCatalogPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<CatalogItem> findCatalogItems(String catalogCode, boolean includeInactive) {
        List<CatalogItemJpaEntity> entities = includeInactive
                ? catalogItemRepository.findByIdCatalogCodeOrderBySortOrderAscLabelAsc(catalogCode)
                : catalogItemRepository.findByIdCatalogCodeAndActiveTrueOrderBySortOrderAscLabelAsc(catalogCode);
        return entities.stream().map(VersionedCatalogPersistenceAdapter::toDomain).toList();
    }

    @Override
    public Optional<CatalogItem> findCatalogItem(String catalogCode, String itemCode) {
        return catalogItemRepository.findById(new CatalogItemJpaId(catalogCode, itemCode))
                .map(VersionedCatalogPersistenceAdapter::toDomain);
    }

    @Override
    public CatalogItem saveCatalogItem(CatalogItem item) {
        CatalogItemJpaEntity saved = catalogItemRepository.save(toEntity(item));
        return toDomain(saved);
    }

    @Override
    public Optional<Boolean> findCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode) {
        CompanyCatalogItemSettingJpaId id = new CompanyCatalogItemSettingJpaId(companyId, catalogCode, itemCode);
        return companySettingRepository.findById(id).map(CompanyCatalogItemSettingJpaEntity::getEnabled);
    }

    @Override
    public void saveCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode, boolean enabled) {
        CompanyCatalogItemSettingJpaEntity setting = CompanyCatalogItemSettingJpaEntity.builder()
                .id(new CompanyCatalogItemSettingJpaId(companyId, catalogCode, itemCode))
                .enabled(enabled)
                .updatedAt(OffsetDateTime.now())
                .build();
        companySettingRepository.save(setting);
    }

    @Override
    public List<Department> findActiveDepartments() {
        return departmentRepository.findByActiveTrueOrderByDepartmentNameAsc().stream()
                .map(VersionedCatalogPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Municipality> findActiveMunicipalitiesByDepartment(String departmentCode) {
        return municipalityRepository.findByDepartmentCodeAndActiveTrueOrderByMunicipalityNameAsc(departmentCode).stream()
                .map(VersionedCatalogPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Municipality> findMunicipality(String municipalityCode) {
        return municipalityRepository.findById(municipalityCode).map(VersionedCatalogPersistenceAdapter::toDomain);
    }

    private static CatalogItem toDomain(CatalogItemJpaEntity entity) {
        return CatalogItem.restore(entity.getId().getCatalogCode(), entity.getId().getItemCode(), entity.getLabel(),
                entity.getDescription(), Boolean.TRUE.equals(entity.getActive()),
                Boolean.TRUE.equals(entity.getRegulatory()), entity.getSource(), entity.getSourceVersion(),
                entity.getValidFrom(), entity.getValidTo(), entity.getSortOrder() == null ? 0 : entity.getSortOrder());
    }

    private static CatalogDefinition toDomain(CatalogDefinitionJpaEntity entity) {
        return CatalogDefinition.restore(entity.getCatalogCode(), entity.getLabel(), entity.getDescription(),
                Boolean.TRUE.equals(entity.getRegulatory()), Boolean.TRUE.equals(entity.getCompanyConfigurable()),
                Boolean.TRUE.equals(entity.getGlobalEditableByRoot()), Boolean.TRUE.equals(entity.getActive()),
                entity.getSortOrder() == null ? 0 : entity.getSortOrder());
    }

    private static CatalogItemJpaEntity toEntity(CatalogItem item) {
        return CatalogItemJpaEntity.builder()
                .id(new CatalogItemJpaId(item.catalogCode(), item.itemCode()))
                .label(item.label())
                .description(item.description())
                .active(item.active())
                .regulatory(item.regulatory())
                .source(item.source())
                .sourceVersion(item.sourceVersion())
                .validFrom(item.validFrom())
                .validTo(item.validTo())
                .sortOrder(item.sortOrder())
                .build();
    }

    private static Department toDomain(DepartmentJpaEntity entity) {
        return Department.restore(entity.getDepartmentCode(), entity.getDepartmentName(),
                Boolean.TRUE.equals(entity.getActive()), entity.getSource(), entity.getSourceVersion(),
                entity.getSortOrder() == null ? 0 : entity.getSortOrder());
    }

    private static Municipality toDomain(MunicipalityJpaEntity entity) {
        return Municipality.restore(entity.getMunicipalityCode(), entity.getDepartmentCode(),
                entity.getMunicipalityName(), Boolean.TRUE.equals(entity.getActive()), entity.getSource(),
                entity.getSourceVersion(), entity.getSortOrder() == null ? 0 : entity.getSortOrder());
    }
}

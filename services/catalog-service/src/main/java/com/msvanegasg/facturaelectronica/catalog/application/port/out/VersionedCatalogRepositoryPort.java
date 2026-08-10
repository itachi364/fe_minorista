package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogDefinition;
import com.msvanegasg.facturaelectronica.catalog.domain.model.CatalogItem;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Department;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Municipality;

public interface VersionedCatalogRepositoryPort {

    List<CatalogDefinition> findActiveCatalogDefinitions();

    Optional<CatalogDefinition> findCatalogDefinition(String catalogCode);

    List<CatalogItem> findActiveCatalogItems(String catalogCode);

    List<CatalogItem> findCatalogItems(String catalogCode, boolean includeInactive);

    Optional<CatalogItem> findCatalogItem(String catalogCode, String itemCode);

    CatalogItem saveCatalogItem(CatalogItem item);

    Optional<Boolean> findCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode);

    void saveCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode, boolean enabled);

    List<Department> findActiveDepartments();

    List<Municipality> findActiveMunicipalitiesByDepartment(String departmentCode);

    Optional<Municipality> findMunicipality(String municipalityCode);
}

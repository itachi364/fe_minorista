package com.msvanegasg.facturaelectronica.catalog.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogDefinitionResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogItemResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.DepartmentResult;
import com.msvanegasg.facturaelectronica.catalog.application.dto.MunicipalityResult;

public interface ManageVersionedCatalogUseCase {

    List<CatalogDefinitionResult> findDefinitions();

    List<CatalogItemResult> findGlobalItems(String catalogCode);

    List<CatalogItemResult> findGlobalItems(String catalogCode, boolean includeInactive);

    CatalogItemResult createGlobalItem(String catalogCode, CatalogItemCommand command);

    CatalogItemResult updateGlobalItem(String catalogCode, String itemCode, CatalogItemCommand command);

    CatalogItemResult setGlobalItemActive(String catalogCode, String itemCode, boolean active);

    List<CatalogItemResult> findCompanyItems(UUID companyId, String catalogCode);

    CatalogItemResult setCompanyItemEnabled(UUID companyId, String catalogCode, String itemCode, boolean enabled);

    List<DepartmentResult> findDepartments();

    List<MunicipalityResult> findMunicipalitiesByDepartment(String departmentCode);

    MunicipalityResult findMunicipality(String municipalityCode);
}

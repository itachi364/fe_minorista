package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.catalog.application.dto.AuditContext;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageVersionedCatalogUseCase;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CatalogItemActivationRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CatalogDefinitionResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CatalogItemRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CatalogItemResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.DepartmentResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.MunicipalityResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class VersionedCatalogController {

    private static final String COMPANY_HEADER = "X-Company-Id";
    private static final String USER_HEADER = "X-User-Id";
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final ManageVersionedCatalogUseCase useCase;

    public VersionedCatalogController(ManageVersionedCatalogUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/catalog-definitions")
    public ResponseEntity<List<CatalogDefinitionResponse>> findCatalogDefinitions() {
        return ResponseEntity.ok(useCase.findDefinitions().stream()
                .map(VersionedCatalogRestMapper::toResponse)
                .toList());
    }

    @GetMapping("/catalogs/{catalogCode}/items")
    public ResponseEntity<List<CatalogItemResponse>> findCatalogItems(@PathVariable String catalogCode,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return ResponseEntity.ok(useCase.findGlobalItems(catalogCode, includeInactive).stream()
                .map(VersionedCatalogRestMapper::toResponse)
                .toList());
    }

    @PostMapping("/catalogs/{catalogCode}/items")
    public ResponseEntity<CatalogItemResponse> createCatalogItem(@PathVariable String catalogCode,
            @RequestHeader(value = COMPANY_HEADER, required = false) UUID companyId,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId,
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId,
            @Valid @RequestBody CatalogItemRequest request) {
        return ResponseEntity.ok(VersionedCatalogRestMapper.toResponse(
                useCase.createGlobalItem(catalogCode, VersionedCatalogRestMapper.toCommand(request),
                        new AuditContext(companyId, userId, correlationId))));
    }

    @PutMapping("/catalogs/{catalogCode}/items/{itemCode}")
    public ResponseEntity<CatalogItemResponse> updateCatalogItem(@PathVariable String catalogCode,
            @PathVariable String itemCode,
            @RequestHeader(value = COMPANY_HEADER, required = false) UUID companyId,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId,
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId,
            @Valid @RequestBody CatalogItemRequest request) {
        return ResponseEntity.ok(VersionedCatalogRestMapper.toResponse(
                useCase.updateGlobalItem(catalogCode, itemCode, VersionedCatalogRestMapper.toCommand(request),
                        new AuditContext(companyId, userId, correlationId))));
    }

    @PutMapping("/catalogs/{catalogCode}/items/{itemCode}/activation")
    public ResponseEntity<CatalogItemResponse> setCatalogItemActive(@PathVariable String catalogCode,
            @PathVariable String itemCode,
            @RequestHeader(value = COMPANY_HEADER, required = false) UUID companyId,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId,
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId,
            @Valid @RequestBody CatalogItemActivationRequest request) {
        return ResponseEntity.ok(VersionedCatalogRestMapper.toResponse(
                useCase.setGlobalItemActive(catalogCode, itemCode, request.active(),
                        new AuditContext(companyId, userId, correlationId))));
    }

    @GetMapping("/company-catalogs/{catalogCode}/items")
    public ResponseEntity<List<CatalogItemResponse>> findCompanyCatalogItems(
            @RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable String catalogCode) {
        return ResponseEntity.ok(useCase.findCompanyItems(companyId, catalogCode).stream()
                .map(VersionedCatalogRestMapper::toResponse)
                .toList());
    }

    @PutMapping("/company-catalogs/{catalogCode}/items/{itemCode}/activation")
    public ResponseEntity<CatalogItemResponse> setCompanyCatalogItemEnabled(
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = USER_HEADER, required = false) UUID userId,
            @RequestHeader(value = CORRELATION_HEADER, required = false) String correlationId,
            @PathVariable String catalogCode,
            @PathVariable String itemCode,
            @Valid @RequestBody CatalogItemActivationRequest request) {
        return ResponseEntity.ok(VersionedCatalogRestMapper.toResponse(
                useCase.setCompanyItemEnabled(companyId, catalogCode, itemCode, request.active(),
                        new AuditContext(companyId, userId, correlationId))));
    }

    @GetMapping("/catalogs/departments")
    public ResponseEntity<List<DepartmentResponse>> findDepartments() {
        return ResponseEntity.ok(useCase.findDepartments().stream()
                .map(VersionedCatalogRestMapper::toResponse)
                .toList());
    }

    @GetMapping("/catalogs/departments/{departmentCode}/municipalities")
    public ResponseEntity<List<MunicipalityResponse>> findMunicipalitiesByDepartment(
            @PathVariable String departmentCode) {
        return ResponseEntity.ok(useCase.findMunicipalitiesByDepartment(departmentCode).stream()
                .map(VersionedCatalogRestMapper::toResponse)
                .toList());
    }

    @GetMapping("/catalogs/municipalities/{municipalityCode}")
    public ResponseEntity<MunicipalityResponse> findMunicipality(@PathVariable String municipalityCode) {
        return ResponseEntity.ok(VersionedCatalogRestMapper.toResponse(useCase.findMunicipality(municipalityCode)));
    }
}

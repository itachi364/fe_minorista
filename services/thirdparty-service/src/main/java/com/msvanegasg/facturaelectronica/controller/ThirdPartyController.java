package com.msvanegasg.facturaelectronica.controller;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageThirdPartyUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.ThirdPartyRestMapper;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.ThirdPartyRequest;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.ThirdPartyResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class ThirdPartyController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final ManageThirdPartyUseCase useCase;

    public ThirdPartyController(ManageThirdPartyUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/third-parties")
    public ResponseEntity<ThirdPartyResponse> create(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody ThirdPartyRequest request) {
        return ResponseEntity.ok(ThirdPartyRestMapper.toResponse(useCase.create(
                ThirdPartyRestMapper.toCommand(companyId, request))));
    }

    @PutMapping("/third-parties/{id}")
    public ResponseEntity<ThirdPartyResponse> update(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID id, @Valid @RequestBody ThirdPartyRequest request) {
        return ResponseEntity.ok(ThirdPartyRestMapper.toResponse(useCase.update(companyId, id,
                ThirdPartyRestMapper.toCommand(companyId, request))));
    }

    @GetMapping("/third-parties/{id}")
    public ResponseEntity<ThirdPartyResponse> findById(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ThirdPartyRestMapper.toResponse(useCase.findById(companyId, id)));
    }

    @GetMapping("/third-parties/by-document")
    public ResponseEntity<ThirdPartyResponse> findByDocument(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam String identificationTypeCode, @RequestParam String identificationNumber) {
        return ResponseEntity.ok(ThirdPartyRestMapper.toResponse(useCase.findByDocument(companyId,
                identificationTypeCode, identificationNumber)));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<ThirdPartyResponse>> findCustomers(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(useCase.findByRole(companyId, ThirdPartyRole.CUSTOMER, active).stream()
                .map(ThirdPartyRestMapper::toResponse)
                .toList());
    }

    @PostMapping("/customers")
    public ResponseEntity<ThirdPartyResponse> createCustomer(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody ThirdPartyRequest request) {
        return ResponseEntity.ok(ThirdPartyRestMapper.toResponse(useCase.create(
                ThirdPartyRestMapper.toCommand(companyId, withRole(request, ThirdPartyRole.CUSTOMER)))));
    }

    @GetMapping("/suppliers")
    public ResponseEntity<List<ThirdPartyResponse>> findSuppliers(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(useCase.findByRole(companyId, ThirdPartyRole.SUPPLIER, active).stream()
                .map(ThirdPartyRestMapper::toResponse)
                .toList());
    }

    @PostMapping("/suppliers")
    public ResponseEntity<ThirdPartyResponse> createSupplier(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody ThirdPartyRequest request) {
        return ResponseEntity.ok(ThirdPartyRestMapper.toResponse(useCase.create(
                ThirdPartyRestMapper.toCommand(companyId, withRole(request, ThirdPartyRole.SUPPLIER)))));
    }

    @PutMapping("/third-parties/{id}/activate")
    public ResponseEntity<Void> activate(@RequestHeader(COMPANY_HEADER) UUID companyId, @PathVariable UUID id) {
        useCase.activate(companyId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/third-parties/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@RequestHeader(COMPANY_HEADER) UUID companyId, @PathVariable UUID id) {
        useCase.deactivate(companyId, id);
        return ResponseEntity.noContent().build();
    }

    private static ThirdPartyRequest withRole(ThirdPartyRequest request, ThirdPartyRole role) {
        Set<ThirdPartyRole> roles = new LinkedHashSet<>(request.roles());
        roles.add(role);
        return new ThirdPartyRequest(request.personType(), request.identificationTypeCode(),
                request.identificationNumber(), request.verificationDigit(), request.fullName(),
                request.businessName(), request.tradeName(), request.email(), request.phone(), request.address(),
                request.municipalityCode(), roles);
    }
}

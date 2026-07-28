package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyLicenseUseCase;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyLicenseRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyLicenseResponse;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyLicenseValidationResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/license")
public class CompanyLicenseController {

    private final ManageCompanyLicenseUseCase manageCompanyLicenseUseCase;

    public CompanyLicenseController(ManageCompanyLicenseUseCase manageCompanyLicenseUseCase) {
        this.manageCompanyLicenseUseCase = manageCompanyLicenseUseCase;
    }

    @PostMapping
    public ResponseEntity<CompanyLicenseResponse> save(@PathVariable UUID companyId,
            @Valid @RequestBody CompanyLicenseRequest request) {
        CompanyLicenseResponse response = CompanyLicenseRestMapper.toResponse(
                manageCompanyLicenseUseCase.save(companyId, CompanyLicenseRestMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public CompanyLicenseResponse findByCompanyId(@PathVariable UUID companyId) {
        return CompanyLicenseRestMapper.toResponse(manageCompanyLicenseUseCase.findByCompanyId(companyId));
    }

    @PutMapping("/activate")
    public CompanyLicenseResponse activate(@PathVariable UUID companyId) {
        return CompanyLicenseRestMapper.toResponse(manageCompanyLicenseUseCase.activate(companyId));
    }

    @PutMapping("/suspend")
    public CompanyLicenseResponse suspend(@PathVariable UUID companyId) {
        return CompanyLicenseRestMapper.toResponse(manageCompanyLicenseUseCase.suspend(companyId));
    }

    @GetMapping("/validation")
    public CompanyLicenseValidationResponse validate(@PathVariable UUID companyId,
            @RequestParam LicenseAction action) {
        return CompanyLicenseRestMapper.toResponse(manageCompanyLicenseUseCase.validate(companyId, action));
    }
}

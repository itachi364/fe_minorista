package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyUseCase;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final ManageCompanyUseCase manageCompanyUseCase;

    public CompanyController(ManageCompanyUseCase manageCompanyUseCase) {
        this.manageCompanyUseCase = manageCompanyUseCase;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = CompanyRestMapper.toResponse(
                manageCompanyUseCase.create(CompanyRestMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CompanyResponse> list() {
        return manageCompanyUseCase.list().stream()
                .map(CompanyRestMapper::toResponse)
                .toList();
    }

    @GetMapping("/{companyId}")
    public CompanyResponse findById(@PathVariable UUID companyId) {
        return CompanyRestMapper.toResponse(manageCompanyUseCase.findById(companyId));
    }

    @PutMapping("/{companyId}/activate")
    public CompanyResponse activate(@PathVariable UUID companyId) {
        return CompanyRestMapper.toResponse(manageCompanyUseCase.activate(companyId));
    }

    @PutMapping("/{companyId}/suspend")
    public CompanyResponse suspend(@PathVariable UUID companyId) {
        return CompanyRestMapper.toResponse(manageCompanyUseCase.suspend(companyId));
    }
}

package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.ManageDianConfigurationUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.DianConfigurationRequest;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.DianConfigurationResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/dian-configuration/companies/{companyId}")
public class DianConfigurationController {

    private final ManageDianConfigurationUseCase useCase;

    public DianConfigurationController(ManageDianConfigurationUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<DianConfigurationResponse> findByCompany(@PathVariable UUID companyId) {
        return useCase.findByCompanyId(companyId)
                .map(DianConfigurationRestMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping
    public DianConfigurationResponse save(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody DianConfigurationRequest request) {
        return DianConfigurationRestMapper.toResponse(useCase.save(DianConfigurationRestMapper.toCommand(companyId,
                userId, request)));
    }

    @PostMapping("/test")
    public DianConfigurationResponse testConnection(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId) {
        return DianConfigurationRestMapper.toResponse(useCase.testConnection(companyId, userId));
    }

    @PostMapping("/activate")
    public DianConfigurationResponse activate(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId) {
        return DianConfigurationRestMapper.toResponse(useCase.activate(companyId, userId));
    }

    @PostMapping("/deactivate")
    public DianConfigurationResponse deactivate(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId) {
        return DianConfigurationRestMapper.toResponse(useCase.deactivate(companyId, userId));
    }
}

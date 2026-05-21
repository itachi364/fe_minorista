package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageServiceSupplyReferenceUseCase;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ServiceSupplyReferenceRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ServiceSupplyReferenceResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class ServiceSupplyReferenceController {

    private final ManageServiceSupplyReferenceUseCase useCase;

    public ServiceSupplyReferenceController(ManageServiceSupplyReferenceUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/service-supply-references")
    public ResponseEntity<ServiceSupplyReferenceResponse> create(@RequestHeader("X-Company-Id") UUID companyId,
            @Valid @RequestBody ServiceSupplyReferenceRequest request) {
        ServiceSupplyReferenceResponse response = InventoryRestMapper
                .toResponse(useCase.create(InventoryRestMapper.toCommand(companyId, request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/products/{serviceProductId}/supply-references")
    public List<ServiceSupplyReferenceResponse> findByService(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID serviceProductId) {
        return InventoryRestMapper
                .toServiceSupplyReferenceResponses(useCase.findByService(companyId, serviceProductId));
    }
}

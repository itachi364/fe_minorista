package com.msvanegasg.facturaelectronica.identity.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.identity.application.dto.OperationalPinCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.OperationalPinResult;
import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageOperationalPinUseCase;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.OperationalPinRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.OperationalPinResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/operational-pin")
public class OperationalPinController {

    private final ManageOperationalPinUseCase manageOperationalPinUseCase;

    public OperationalPinController(ManageOperationalPinUseCase manageOperationalPinUseCase) {
        this.manageOperationalPinUseCase = manageOperationalPinUseCase;
    }

    @GetMapping
    public OperationalPinResponse status(@PathVariable UUID companyId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return toResponse(manageOperationalPinUseCase.findStatus(companyId, authorizationHeader));
    }

    @PutMapping
    public OperationalPinResponse configure(@PathVariable UUID companyId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody OperationalPinRequest request) {
        return toResponse(manageOperationalPinUseCase.configure(
                new OperationalPinCommand(companyId, request.pin(), authorizationHeader)));
    }

    @PostMapping("/verify")
    public OperationalPinResponse verify(@PathVariable UUID companyId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody OperationalPinRequest request) {
        return toResponse(manageOperationalPinUseCase.verify(
                new OperationalPinCommand(companyId, request.pin(), authorizationHeader)));
    }

    @PutMapping("/unlock")
    public OperationalPinResponse unlock(@PathVariable UUID companyId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return toResponse(manageOperationalPinUseCase.unlock(companyId, authorizationHeader));
    }

    private static OperationalPinResponse toResponse(OperationalPinResult result) {
        return new OperationalPinResponse(result.companyId(), result.configured(), result.valid(), result.locked(),
                result.mustChange(), result.remainingAttempts(), result.updatedAt());
    }
}

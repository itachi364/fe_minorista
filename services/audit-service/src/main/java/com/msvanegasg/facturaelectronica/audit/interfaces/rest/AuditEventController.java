package com.msvanegasg.facturaelectronica.audit.interfaces.rest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventQuery;
import com.msvanegasg.facturaelectronica.audit.application.port.in.QueryAuditEventsUseCase;
import com.msvanegasg.facturaelectronica.audit.application.port.in.RegisterAuditEventUseCase;
import com.msvanegasg.facturaelectronica.audit.interfaces.rest.dto.AuditEventRequest;
import com.msvanegasg.facturaelectronica.audit.interfaces.rest.dto.AuditEventResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/audit-events")
public class AuditEventController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final RegisterAuditEventUseCase registerAuditEventUseCase;
    private final QueryAuditEventsUseCase queryAuditEventsUseCase;

    public AuditEventController(RegisterAuditEventUseCase registerAuditEventUseCase,
            QueryAuditEventsUseCase queryAuditEventsUseCase) {
        this.registerAuditEventUseCase = registerAuditEventUseCase;
        this.queryAuditEventsUseCase = queryAuditEventsUseCase;
    }

    @PostMapping
    public ResponseEntity<AuditEventResponse> register(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody AuditEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuditRestMapper.toResponse(
                        registerAuditEventUseCase.register(AuditRestMapper.toCommand(companyId, request))));
    }

    @GetMapping
    public ResponseEntity<List<AuditEventResponse>> find(
            @RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(required = false) UUID userId) {
        return ResponseEntity.ok(queryAuditEventsUseCase.find(
                new AuditEventQuery(companyId, resourceType, resourceId, from, to, userId))
                .stream()
                .map(AuditRestMapper::toResponse)
                .toList());
    }
}

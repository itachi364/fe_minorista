package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalEventCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateFiscalLegalSourceCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageFiscalLegalSourcesUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCatalogWarning;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSourceEvent;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalTimeline;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.FiscalLegalEventRequest;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.FiscalLegalSourceRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/fiscal-legal-sources")
public class FiscalLegalSourceController {
    private final ManageFiscalLegalSourcesUseCase useCase;

    public FiscalLegalSourceController(ManageFiscalLegalSourcesUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public List<FiscalLegalTimeline> timelines(@RequestParam(required = false) LocalDate evaluatedOn) {
        return useCase.timelines(evaluatedOn);
    }

    @GetMapping("/warnings")
    public List<FiscalCatalogWarning> warnings(@RequestParam(required = false) LocalDate evaluatedOn) {
        return useCase.warnings(evaluatedOn);
    }

    @PostMapping
    public ResponseEntity<FiscalLegalSource> create(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody FiscalLegalSourceRequest request) {
        FiscalLegalSource source = useCase.create(new CreateFiscalLegalSourceCommand(request.code(), request.title(),
                request.authority(), request.officialUrl(), request.issuedOn(), request.reviewDueOn(), userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(source);
    }

    @PostMapping("/{sourceId}/events")
    public ResponseEntity<FiscalLegalSourceEvent> addEvent(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID sourceId,
            @Valid @RequestBody FiscalLegalEventRequest request) {
        FiscalLegalSourceEvent event = useCase.addEvent(new CreateFiscalLegalEventCommand(sourceId,
                request.eventType(), request.effectiveFrom(), request.effectiveTo(), request.reference(),
                request.officialUrl(), request.notes(), userId));
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }
}

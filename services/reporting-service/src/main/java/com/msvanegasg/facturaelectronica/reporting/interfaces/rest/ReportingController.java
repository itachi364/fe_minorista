package com.msvanegasg.facturaelectronica.reporting.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOptionsResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryCommand;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryResult;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportsUseCase;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportDefinition;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto.ReportQueryRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class ReportingController {

    private final ManageReportsUseCase useCase;

    public ReportingController(ManageReportsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/report-definitions")
    public List<ReportDefinition> definitions() {
        return useCase.definitions();
    }

    @GetMapping("/reports/{reportCode}/options")
    public ReportOptionsResult options(@PathVariable String reportCode,
            @RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        return useCase.options(companyId, reportCode, authorizationHeader);
    }

    @PostMapping("/reports/query")
    public ReportQueryResult query(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody ReportQueryRequest request) {
        return useCase.query(new ReportQueryCommand(companyId, request.reportCode(), request.from(), request.to(),
                request.filters(), request.chartType(), authorizationHeader));
    }

    @PostMapping("/reports/export")
    public ResponseEntity<byte[]> export(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @RequestBody @Valid ReportQueryRequest request,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "CSV") ReportExportFormat format) {
        ReportExportResult result = useCase.export(new ReportQueryCommand(companyId, request.reportCode(),
                request.from(), request.to(), request.filters(), request.chartType(), authorizationHeader), format);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(result.filename()).build().toString())
                .body(result.content());
    }
}

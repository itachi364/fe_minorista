package com.msvanegasg.facturaelectronica.reporting.interfaces.rest;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.reporting.application.dto.CreateReportExportJobCommand;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportExportJobsUseCase;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto.ReportDownloadLinkResponse;
import com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto.ReportExportJobRequest;
import com.msvanegasg.facturaelectronica.reporting.interfaces.rest.dto.ReportExportJobResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/reports/export-jobs")
public class ReportExportJobController {

    private final ManageReportExportJobsUseCase useCase;

    public ReportExportJobController(ManageReportExportJobsUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<ReportExportJobResponse> create(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody ReportExportJobRequest request) {
        ReportExportJobResponse response = ReportExportJobResponse.from(useCase.create(
                new CreateReportExportJobCommand(companyId, userId, request.reportCode(),
                        request.format() == null ? ReportExportFormat.XLS : request.format(), request.chartType(),
                        request.from(), request.to(), request.filters(), request.notifyByEmail(),
                        authorizationHeader)));
        return ResponseEntity.created(URI.create("/api/v1/reports/export-jobs/" + response.jobId())).body(response);
    }

    @GetMapping
    public List<ReportExportJobResponse> find(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(required = false) UUID requestedByUserId,
            @RequestParam(required = false) ReportExportJobStatus status) {
        return useCase.find(companyId, requestedByUserId, status).stream().map(ReportExportJobResponse::from).toList();
    }

    @GetMapping("/{jobId}")
    public ReportExportJobResponse findById(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID jobId) {
        return ReportExportJobResponse.from(useCase.findById(companyId, jobId));
    }

    @PostMapping("/{jobId}/download-link")
    public ReportDownloadLinkResponse createDownloadLink(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID jobId) {
        return ReportDownloadLinkResponse.from(useCase.createDownloadLink(companyId, jobId));
    }
}

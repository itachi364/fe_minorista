package com.msvanegasg.facturaelectronica.reporting.interfaces.rest;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportExportJobsUseCase;

@RestController
public class ReportDownloadController {

    private final ManageReportExportJobsUseCase useCase;

    public ReportDownloadController(ManageReportExportJobsUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/reportes/descarga/{token}")
    public ResponseEntity<byte[]> download(@PathVariable String token) {
        var result = useCase.downloadByToken(token);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(result.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(result.filename()).build().toString())
                .header("X-Report-Presigned-Ttl-Seconds", String.valueOf(result.presignedTtlSeconds()))
                .body(result.content());
    }
}

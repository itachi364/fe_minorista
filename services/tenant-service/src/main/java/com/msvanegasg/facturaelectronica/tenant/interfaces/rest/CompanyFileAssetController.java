package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyFileContentResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyFileAssetUseCase;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileCategory;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyFileAssetResponse;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/files")
public class CompanyFileAssetController {

    private final ManageCompanyFileAssetUseCase useCase;

    public CompanyFileAssetController(ManageCompanyFileAssetUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyFileAssetResponse> upload(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId,
            @RequestParam CompanyFileCategory category,
            @RequestParam MultipartFile file) throws IOException {
        CompanyFileAssetCommand command = new CompanyFileAssetCommand(category, file.getOriginalFilename(),
                file.getContentType(), file.getBytes(), userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CompanyFileAssetRestMapper.toResponse(useCase.upload(companyId, command)));
    }

    @GetMapping("/{assetId}")
    public ResponseEntity<byte[]> read(@PathVariable UUID companyId, @PathVariable UUID assetId) {
        CompanyFileContentResult file = useCase.read(companyId, assetId);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.ETAG, "\"" + file.contentHash() + "\"")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.originalFilename()).build().toString())
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.content());
    }
}

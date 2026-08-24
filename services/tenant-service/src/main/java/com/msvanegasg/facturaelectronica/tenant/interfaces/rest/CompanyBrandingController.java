package com.msvanegasg.facturaelectronica.tenant.interfaces.rest;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyBrandingAssetResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyBrandingUseCase;
import com.msvanegasg.facturaelectronica.tenant.domain.model.BrandingAssetPurpose;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyBrandingRequest;
import com.msvanegasg.facturaelectronica.tenant.interfaces.rest.dto.CompanyBrandingResponse;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/branding")
public class CompanyBrandingController {

    private final ManageCompanyBrandingUseCase useCase;

    public CompanyBrandingController(ManageCompanyBrandingUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public CompanyBrandingResponse find(@PathVariable UUID companyId) {
        return CompanyBrandingRestMapper.toResponse(useCase.findByCompanyId(companyId));
    }

    @PutMapping
    public CompanyBrandingResponse update(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId,
            @RequestBody CompanyBrandingRequest request) {
        return CompanyBrandingRestMapper.toResponse(
                useCase.update(companyId, CompanyBrandingRestMapper.toCommand(request, userId)));
    }

    @PostMapping(path = "/assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompanyBrandingResponse uploadAsset(@PathVariable UUID companyId,
            @RequestHeader(name = "X-User-Id", required = false) UUID userId,
            @RequestParam BrandingAssetPurpose purpose,
            @RequestParam MultipartFile file) throws IOException {
        return CompanyBrandingRestMapper.toResponse(useCase.uploadAsset(companyId,
                new CompanyBrandingAssetCommand(purpose, file.getOriginalFilename(), file.getContentType(),
                        file.getBytes(), userId)));
    }

    @GetMapping("/assets/{purpose}")
    public ResponseEntity<byte[]> readAsset(@PathVariable UUID companyId, @PathVariable BrandingAssetPurpose purpose) {
        CompanyBrandingAssetResult asset = useCase.readAsset(companyId, purpose);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .header(HttpHeaders.ETAG, "\"" + asset.contentHash() + "\"")
                .contentType(MediaType.parseMediaType(asset.contentType()))
                .body(asset.content());
    }
}

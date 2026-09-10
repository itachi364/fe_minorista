package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateMunicipalFiscalPackageCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalCsvValidationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.MunicipalReteicaRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageMunicipalFiscalPackagesUseCase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalFiscalRulePackage;
import com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto.MunicipalFiscalPackageRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/fiscal-rule-packages")
public class MunicipalFiscalPackageController {
    private static final String CSV_HEADER = "municipalityDivipolaCode,packageCode,version,operationType,conceptCode,"
            + "ciiuCode,rate,thresholdUnit,thresholdValue,thresholdOperator,calculationBase,validFrom,validTo,"
            + "legalReference,officialSourceUrl\r\n";
    private final ManageMunicipalFiscalPackagesUseCase useCase;

    public MunicipalFiscalPackageController(ManageMunicipalFiscalPackagesUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/municipalities")
    public List<MunicipalFiscalRulePackage> findAll() {
        return useCase.findAll();
    }

    @GetMapping(value = "/municipalities/template.csv", produces = "text/csv")
    public ResponseEntity<byte[]> template() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename("plantilla-reteica.csv").build());
        return ResponseEntity.ok().headers(headers).body(CSV_HEADER.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/municipalities")
    public ResponseEntity<MunicipalFiscalRulePackage> create(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @Valid @RequestBody MunicipalFiscalPackageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createDraft(toCommand(request, userId)));
    }

    @PostMapping(value = "/municipalities/validate-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FiscalCsvValidationResult validateCsv(@RequestPart("file") MultipartFile file) throws IOException {
        return useCase.validateCsv(file.getOriginalFilename(), file.getBytes());
    }

    @PostMapping(value = "/municipalities/import-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<MunicipalFiscalRulePackage>> importCsv(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(useCase.importCsv(file.getOriginalFilename(), file.getBytes(), userId));
    }

    @PostMapping("/{packageId}/publish")
    public MunicipalFiscalRulePackage publish(
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @PathVariable UUID packageId) {
        return useCase.publish(packageId, userId);
    }

    private static CreateMunicipalFiscalPackageCommand toCommand(MunicipalFiscalPackageRequest request, UUID userId) {
        List<MunicipalReteicaRuleCommand> rules = request.rules().stream()
                .map(rule -> new MunicipalReteicaRuleCommand(rule.operationType(), rule.conceptCode(), rule.ciiuCode(),
                        rule.rate(), rule.thresholdUnit(), rule.thresholdValue(), rule.thresholdOperator(),
                        rule.calculationBase()))
                .toList();
        return new CreateMunicipalFiscalPackageCommand(request.municipalityCode(), request.packageCode(),
                request.version(), request.validFrom(), request.validTo(), request.legalReference(),
                request.officialSourceUrl(), rules, userId);
    }
}

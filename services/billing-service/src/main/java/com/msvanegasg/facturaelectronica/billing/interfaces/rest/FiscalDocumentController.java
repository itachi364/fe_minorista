package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentQuery;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicDocumentResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.FiscalArtifactResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.FiscalEventResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleResponse;

@RestController
@RequestMapping("/api/v1")
public class FiscalDocumentController {

    private final ManageSaleUseCase saleUseCase;

    public FiscalDocumentController(ManageSaleUseCase saleUseCase) {
        this.saleUseCase = saleUseCase;
    }

    @PostMapping("/electronic-pos")
    public SaleResponse createElectronicPos(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam UUID saleId) {
        return BillingRestMapper.toResponse(saleUseCase.confirm(companyId, saleId, idempotencyKey));
    }

    @PostMapping("/electronic-pos/{documentId}/submit")
    public ElectronicDocumentResponse submitElectronicPos(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID documentId) {
        return BillingRestMapper.toDocumentResponse(saleUseCase.findElectronicDocument(companyId, documentId));
    }

    @GetMapping("/electronic-pos/{documentId}")
    public ElectronicDocumentResponse findElectronicPos(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID documentId) {
        return BillingRestMapper.toDocumentResponse(saleUseCase.findElectronicDocument(companyId, documentId));
    }

    @PostMapping("/electronic-invoices")
    public SaleResponse createElectronicInvoice(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam UUID saleId) {
        return BillingRestMapper.toResponse(saleUseCase.confirm(companyId, saleId, idempotencyKey));
    }

    @PostMapping("/electronic-invoices/{documentId}/issue")
    public ElectronicDocumentResponse issueElectronicInvoice(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID documentId) {
        return BillingRestMapper.toDocumentResponse(saleUseCase.findElectronicDocument(companyId, documentId));
    }

    @GetMapping("/electronic-invoices/{documentId}")
    public ElectronicDocumentResponse findElectronicInvoice(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID documentId) {
        return BillingRestMapper.toDocumentResponse(saleUseCase.findElectronicDocument(companyId, documentId));
    }

    @GetMapping("/electronic-invoices")
    public List<ElectronicDocumentResponse> findElectronicInvoices(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(required = false) ElectronicDocumentStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) Long number,
            @RequestParam(required = false) String cufeCude) {
        return saleUseCase.findElectronicDocuments(new ElectronicDocumentQuery(companyId,
                ElectronicDocumentType.ELECTRONIC_INVOICE, status, customerId, from, to, prefix, number, cufeCude))
                .stream().map(BillingRestMapper::toDocumentResponse).toList();
    }

    @GetMapping("/electronic-invoices/{documentId}/artifacts")
    public List<FiscalArtifactResponse> findArtifacts(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID documentId) {
        return saleUseCase.findArtifacts(companyId, documentId).stream().map(BillingRestMapper::toResponse).toList();
    }

    @GetMapping("/electronic-invoices/{documentId}/events")
    public List<FiscalEventResponse> findEvents(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID documentId) {
        return saleUseCase.findFiscalEvents(companyId, documentId).stream().map(BillingRestMapper::toResponse).toList();
    }
}
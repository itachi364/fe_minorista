package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentQuery;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleQuery;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.PaymentMethodCode;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.ElectronicDocumentResponse;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleResponse;

@RestController
@RequestMapping("/api/v1/reports")
public class BillingReportController {

    private final ManageSaleUseCase saleUseCase;

    public BillingReportController(ManageSaleUseCase saleUseCase) {
        this.saleUseCase = saleUseCase;
    }

    @GetMapping("/sales")
    public List<SaleResponse> sales(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(required = false) SaleStatus status,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) PaymentMethodCode paymentMethodCode,
            @RequestParam(required = false) ElectronicDocumentStatus documentStatus) {
        return saleUseCase.find(new SaleQuery(companyId, status, from, to, sellerId, customerId, productId,
                paymentMethodCode, documentStatus)).stream()
                .map(BillingRestMapper::toResponse)
                .toList();
    }

    @GetMapping("/electronic-documents")
    public List<ElectronicDocumentResponse> electronicDocuments(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(required = false) ElectronicDocumentType documentType,
            @RequestParam(required = false) ElectronicDocumentStatus status,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) Long number,
            @RequestParam(required = false) String cufeCude) {
        return saleUseCase.findElectronicDocuments(new ElectronicDocumentQuery(companyId, documentType, status,
                customerId, from, to, prefix, number, cufeCude)).stream()
                .map(BillingRestMapper::toDocumentResponse)
                .toList();
    }
}

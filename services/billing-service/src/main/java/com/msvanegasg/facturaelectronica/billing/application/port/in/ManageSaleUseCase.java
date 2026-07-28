package com.msvanegasg.facturaelectronica.billing.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateSaleCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentQuery;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalArtifactResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalEventResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleQuery;
import com.msvanegasg.facturaelectronica.billing.application.dto.SaleResult;

public interface ManageSaleUseCase {

    SaleResult create(CreateSaleCommand command);

    SaleResult confirm(UUID companyId, UUID saleId, String idempotencyKey);

    List<SaleResult> find(SaleQuery query);

    SaleResult findById(UUID companyId, UUID saleId);

    List<ElectronicDocumentResult> findElectronicDocuments(ElectronicDocumentQuery query);

    ElectronicDocumentResult findElectronicDocument(UUID companyId, UUID documentId);

    List<FiscalArtifactResult> findArtifacts(UUID companyId, UUID documentId);

    List<FiscalEventResult> findFiscalEvents(UUID companyId, UUID documentId);
}
package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import java.util.List;

import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineCommand;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseLineRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseRequest;

public final class PurchaseRestMapper {

    private PurchaseRestMapper() {
    }

    public static PurchaseCommand toCommand(PurchaseRequest dto) {
        return new PurchaseCommand(
                dto.getNumeroDocumento(),
                dto.getTipoDocumentoId(),
                dto.getSubtotal(),
                dto.getIvaTotal(),
                dto.getTotal(),
                dto.getUrlEvidencia(),
                dto.getDetalles().stream()
                        .map(PurchaseRestMapper::toLineCommand)
                        .toList());
    }

    public static PurchaseRequest toResponse(Purchase purchase) {
        return PurchaseRequest.builder()
                .fecha(purchase.date())
                .subtotal(purchase.subtotal())
                .ivaTotal(purchase.taxTotal())
                .total(purchase.total())
                .urlEvidencia(purchase.evidenceUrl())
                .detalles(toDetailDtos(purchase.lines()))
                .build();
    }

    public static PurchaseLineRequest toResponse(PurchaseLine line) {
        return PurchaseLineRequest.builder()
                .codigoBarras(line.barcode())
                .cantidad(line.quantity())
                .precioUnitario(line.unitPrice())
                .subtotal(line.subtotal())
                .iva(line.tax())
                .totalLinea(line.lineTotal())
                .build();
    }

    private static PurchaseLineCommand toLineCommand(PurchaseLineRequest dto) {
        return new PurchaseLineCommand(
                dto.getCodigoBarras(),
                dto.getCantidad(),
                dto.getPrecioUnitario(),
                dto.getSubtotal(),
                dto.getIva(),
                dto.getTotalLinea());
    }

    private static List<PurchaseLineRequest> toDetailDtos(List<PurchaseLine> lines) {
        return lines.stream().map(PurchaseRestMapper::toResponse).toList();
    }
}

package com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PurchaseRequest(
        UUID supplierId,
        @DecimalMin(value = "0.0") BigDecimal subtotal,
        @DecimalMin(value = "0.0") BigDecimal taxTotal,
        @NotNull @DecimalMin(value = "0.0") BigDecimal total,
        PaymentCondition paymentCondition,
        LocalDate dueDate,
        @Size(max = 700)
        @Pattern(regexp = "^(https?://|/api/v1/companies/).+", message = "La evidencia debe ser una URL http/https o una referencia interna valida.")
        String evidenceUrl,
        @NotEmpty List<@Valid PurchaseLineRequest> lines) {
}

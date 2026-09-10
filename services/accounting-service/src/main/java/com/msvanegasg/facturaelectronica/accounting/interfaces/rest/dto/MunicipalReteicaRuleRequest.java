package com.msvanegasg.facturaelectronica.accounting.interfaces.rest.dto;

import java.math.BigDecimal;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record MunicipalReteicaRuleRequest(
        @NotNull FiscalOperationType operationType,
        String conceptCode,
        String ciiuCode,
        @NotNull @DecimalMin("0") @DecimalMax("1") BigDecimal rate,
        @NotNull FiscalThresholdUnit thresholdUnit,
        @NotNull @DecimalMin("0") BigDecimal thresholdValue,
        @NotNull FiscalThresholdOperator thresholdOperator,
        @NotNull FiscalCalculationBase calculationBase) {
}

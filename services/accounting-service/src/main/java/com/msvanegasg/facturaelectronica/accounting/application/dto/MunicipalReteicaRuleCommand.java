package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;

import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;

public record MunicipalReteicaRuleCommand(
        FiscalOperationType operationType,
        String conceptCode,
        String ciiuCode,
        BigDecimal rate,
        FiscalThresholdUnit thresholdUnit,
        BigDecimal thresholdValue,
        FiscalThresholdOperator thresholdOperator,
        FiscalCalculationBase calculationBase) {
}

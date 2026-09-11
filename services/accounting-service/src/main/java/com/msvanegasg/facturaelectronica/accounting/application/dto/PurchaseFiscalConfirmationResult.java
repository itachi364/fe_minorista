package com.msvanegasg.facturaelectronica.accounting.application.dto;

public record PurchaseFiscalConfirmationResult(
        FiscalDocumentCalculationResult fiscalCalculation,
        AccountingEntryResult accountingEntry,
        AccountsPayableResult accountsPayable,
        String status) {
}

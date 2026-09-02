package com.msvanegasg.facturaelectronica.tenant.domain.model;

public enum CompanyFileCategory {
    INVOICE("facturas"),
    LOGO("logos"),
    BACKGROUND("fondos"),
    PURCHASE_EVIDENCE("facturas"),
    EXPENSE_EVIDENCE("gastos"),
    OTHER("otros");

    private final String folderName;

    CompanyFileCategory(String folderName) {
        this.folderName = folderName;
    }

    public String folderName() {
        return folderName;
    }
}

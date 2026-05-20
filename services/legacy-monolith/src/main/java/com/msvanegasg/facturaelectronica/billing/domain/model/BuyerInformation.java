package com.msvanegasg.facturaelectronica.billing.domain.model;

public record BuyerInformation(
        String name,
        String documentType,
        String documentNumber) {

    public BuyerInformation {
        name = blankToNull(name);
        documentType = blankToNull(documentType);
        documentNumber = blankToNull(documentNumber);

        boolean anyValuePresent = name != null || documentType != null || documentNumber != null;
        boolean allValuesPresent = name != null && documentType != null && documentNumber != null;
        if (anyValuePresent && !allValuesPresent) {
            throw new IllegalArgumentException("buyer name, documentType and documentNumber must be provided together");
        }
    }

    public boolean isPresent() {
        return name != null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

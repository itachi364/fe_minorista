package com.msvanegasg.facturaelectronica.billing.application.dto;

public record LicensePolicy(String planCode, Integer maxUsers, Integer maxMonthlyDocuments) {

    public LicensePolicy(Integer maxUsers, Integer maxMonthlyDocuments) {
        this(null, maxUsers, maxMonthlyDocuments);
    }

    public boolean isPos() {
        return "POS".equalsIgnoreCase(planCode);
    }

    public static LicensePolicy unlimited() {
        return new LicensePolicy(null, null, null);
    }
}

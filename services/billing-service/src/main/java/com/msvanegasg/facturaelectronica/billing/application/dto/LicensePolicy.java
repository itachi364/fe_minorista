package com.msvanegasg.facturaelectronica.billing.application.dto;

public record LicensePolicy(Integer maxUsers, Integer maxMonthlyDocuments) {

    public static LicensePolicy unlimited() {
        return new LicensePolicy(null, null);
    }
}

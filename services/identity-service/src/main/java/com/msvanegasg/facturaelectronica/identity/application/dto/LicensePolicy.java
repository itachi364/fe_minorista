package com.msvanegasg.facturaelectronica.identity.application.dto;

public record LicensePolicy(Integer maxUsers, Integer maxMonthlyDocuments) {

    public static LicensePolicy unlimited() {
        return new LicensePolicy(null, null);
    }
}

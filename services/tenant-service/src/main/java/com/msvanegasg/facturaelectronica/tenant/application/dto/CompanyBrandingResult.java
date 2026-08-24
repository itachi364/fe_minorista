package com.msvanegasg.facturaelectronica.tenant.application.dto;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyBranding;

public record CompanyBrandingResult(
        UUID companyId,
        String displayName,
        String primaryColor,
        String accentColor,
        String mainLogoUrl,
        String headerLogoUrl,
        String loginLogoUrl,
        String faviconUrl,
        Instant updatedAt) {

    public static CompanyBrandingResult from(CompanyBranding branding) {
        UUID companyId = branding.companyId();
        return new CompanyBrandingResult(companyId, branding.displayName(), branding.primaryColor(),
                branding.accentColor(), assetUrl(companyId, "MAIN_LOGO", branding.mainLogoHash()),
                assetUrl(companyId, "HEADER_LOGO", branding.headerLogoHash()),
                assetUrl(companyId, "LOGIN_LOGO", branding.loginLogoHash()),
                assetUrl(companyId, "FAVICON", branding.faviconHash()), branding.updatedAt());
    }

    private static String assetUrl(UUID companyId, String purpose, String hash) {
        if (hash == null || hash.isBlank()) {
            return null;
        }
        return "/api/v1/companies/%s/branding/assets/%s?hash=%s".formatted(companyId, purpose, hash);
    }
}

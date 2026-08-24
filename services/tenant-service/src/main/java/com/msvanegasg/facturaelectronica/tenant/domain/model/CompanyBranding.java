package com.msvanegasg.facturaelectronica.tenant.domain.model;

import java.time.Instant;
import java.util.UUID;

public record CompanyBranding(
        UUID companyId,
        String displayName,
        String primaryColor,
        String accentColor,
        String mainLogoStorageKey,
        String headerLogoStorageKey,
        String loginLogoStorageKey,
        String faviconStorageKey,
        String mainLogoContentType,
        String headerLogoContentType,
        String loginLogoContentType,
        String faviconContentType,
        String mainLogoHash,
        String headerLogoHash,
        String loginLogoHash,
        String faviconHash,
        UUID updatedBy,
        Instant updatedAt) {

    public static CompanyBranding empty(UUID companyId, Instant now) {
        return new CompanyBranding(companyId, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, now);
    }

    public CompanyBranding updateMetadata(String displayName, String primaryColor, String accentColor, UUID updatedBy,
            Instant updatedAt) {
        return new CompanyBranding(companyId, blankToNull(displayName), blankToNull(primaryColor),
                blankToNull(accentColor), mainLogoStorageKey, headerLogoStorageKey, loginLogoStorageKey,
                faviconStorageKey, mainLogoContentType, headerLogoContentType, loginLogoContentType,
                faviconContentType, mainLogoHash, headerLogoHash, loginLogoHash, faviconHash, updatedBy, updatedAt);
    }

    public CompanyBranding updateAsset(BrandingAssetPurpose purpose, String storageKey, String contentType,
            String contentHash, UUID updatedBy, Instant updatedAt) {
        return switch (purpose) {
            case MAIN_LOGO -> new CompanyBranding(companyId, displayName, primaryColor, accentColor, storageKey,
                    headerLogoStorageKey, loginLogoStorageKey, faviconStorageKey, contentType, headerLogoContentType,
                    loginLogoContentType, faviconContentType, contentHash, headerLogoHash, loginLogoHash, faviconHash,
                    updatedBy, updatedAt);
            case HEADER_LOGO -> new CompanyBranding(companyId, displayName, primaryColor, accentColor,
                    mainLogoStorageKey, storageKey, loginLogoStorageKey, faviconStorageKey, mainLogoContentType,
                    contentType, loginLogoContentType, faviconContentType, mainLogoHash, contentHash, loginLogoHash,
                    faviconHash, updatedBy, updatedAt);
            case LOGIN_LOGO -> new CompanyBranding(companyId, displayName, primaryColor, accentColor,
                    mainLogoStorageKey, headerLogoStorageKey, storageKey, faviconStorageKey, mainLogoContentType,
                    headerLogoContentType, contentType, faviconContentType, mainLogoHash, headerLogoHash, contentHash,
                    faviconHash, updatedBy, updatedAt);
            case FAVICON -> new CompanyBranding(companyId, displayName, primaryColor, accentColor, mainLogoStorageKey,
                    headerLogoStorageKey, loginLogoStorageKey, storageKey, mainLogoContentType, headerLogoContentType,
                    loginLogoContentType, contentType, mainLogoHash, headerLogoHash, loginLogoHash, contentHash,
                    updatedBy, updatedAt);
        };
    }

    public String storageKeyFor(BrandingAssetPurpose purpose) {
        return switch (purpose) {
            case MAIN_LOGO -> mainLogoStorageKey;
            case HEADER_LOGO -> headerLogoStorageKey;
            case LOGIN_LOGO -> loginLogoStorageKey;
            case FAVICON -> faviconStorageKey;
        };
    }

    public String contentTypeFor(BrandingAssetPurpose purpose) {
        return switch (purpose) {
            case MAIN_LOGO -> mainLogoContentType;
            case HEADER_LOGO -> headerLogoContentType;
            case LOGIN_LOGO -> loginLogoContentType;
            case FAVICON -> faviconContentType;
        };
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

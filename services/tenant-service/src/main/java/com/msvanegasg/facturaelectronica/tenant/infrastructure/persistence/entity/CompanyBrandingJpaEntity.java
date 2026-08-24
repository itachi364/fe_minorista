package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_branding", schema = "tenant")
public class CompanyBrandingJpaEntity {

    @Id
    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "display_name", length = 180)
    private String displayName;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "accent_color", length = 20)
    private String accentColor;

    @Column(name = "main_logo_storage_key", length = 500)
    private String mainLogoStorageKey;

    @Column(name = "header_logo_storage_key", length = 500)
    private String headerLogoStorageKey;

    @Column(name = "login_logo_storage_key", length = 500)
    private String loginLogoStorageKey;

    @Column(name = "favicon_storage_key", length = 500)
    private String faviconStorageKey;

    @Column(name = "main_logo_content_type", length = 80)
    private String mainLogoContentType;

    @Column(name = "header_logo_content_type", length = 80)
    private String headerLogoContentType;

    @Column(name = "login_logo_content_type", length = 80)
    private String loginLogoContentType;

    @Column(name = "favicon_content_type", length = 80)
    private String faviconContentType;

    @Column(name = "main_logo_hash", length = 120)
    private String mainLogoHash;

    @Column(name = "header_logo_hash", length = 120)
    private String headerLogoHash;

    @Column(name = "login_logo_hash", length = 120)
    private String loginLogoHash;

    @Column(name = "favicon_hash", length = 120)
    private String faviconHash;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CompanyBrandingJpaEntity() {
    }

    public CompanyBrandingJpaEntity(UUID companyId, String displayName, String primaryColor, String accentColor,
            String mainLogoStorageKey, String headerLogoStorageKey, String loginLogoStorageKey,
            String faviconStorageKey, String mainLogoContentType, String headerLogoContentType,
            String loginLogoContentType, String faviconContentType, String mainLogoHash, String headerLogoHash,
            String loginLogoHash, String faviconHash, UUID updatedBy, Instant updatedAt) {
        this.companyId = companyId;
        this.displayName = displayName;
        this.primaryColor = primaryColor;
        this.accentColor = accentColor;
        this.mainLogoStorageKey = mainLogoStorageKey;
        this.headerLogoStorageKey = headerLogoStorageKey;
        this.loginLogoStorageKey = loginLogoStorageKey;
        this.faviconStorageKey = faviconStorageKey;
        this.mainLogoContentType = mainLogoContentType;
        this.headerLogoContentType = headerLogoContentType;
        this.loginLogoContentType = loginLogoContentType;
        this.faviconContentType = faviconContentType;
        this.mainLogoHash = mainLogoHash;
        this.headerLogoHash = headerLogoHash;
        this.loginLogoHash = loginLogoHash;
        this.faviconHash = faviconHash;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public String getMainLogoStorageKey() {
        return mainLogoStorageKey;
    }

    public String getHeaderLogoStorageKey() {
        return headerLogoStorageKey;
    }

    public String getLoginLogoStorageKey() {
        return loginLogoStorageKey;
    }

    public String getFaviconStorageKey() {
        return faviconStorageKey;
    }

    public String getMainLogoContentType() {
        return mainLogoContentType;
    }

    public String getHeaderLogoContentType() {
        return headerLogoContentType;
    }

    public String getLoginLogoContentType() {
        return loginLogoContentType;
    }

    public String getFaviconContentType() {
        return faviconContentType;
    }

    public String getMainLogoHash() {
        return mainLogoHash;
    }

    public String getHeaderLogoHash() {
        return headerLogoHash;
    }

    public String getLoginLogoHash() {
        return loginLogoHash;
    }

    public String getFaviconHash() {
        return faviconHash;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyFileCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_file_asset", schema = "tenant")
public class CompanyFileAssetJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CompanyFileCategory category;
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;
    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;
    @Column(name = "file_size", nullable = false)
    private long fileSize;
    @Column(name = "content_hash", nullable = false, length = 128)
    private String contentHash;
    @Column(name = "uploaded_by")
    private UUID uploadedBy;
    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public CompanyFileCategory getCategory() { return category; }
    public void setCategory(CompanyFileCategory category) { this.category = category; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public UUID getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; }
    public Instant getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Instant uploadedAt) { this.uploadedAt = uploadedAt; }
}

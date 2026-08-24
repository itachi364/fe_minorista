package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianArtifactType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dian_submission_artifact", schema = "dian_provider")
public class DianSubmissionArtifactJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;
    @Column(name = "document_id", nullable = false)
    private UUID documentId;
    @Enumerated(EnumType.STRING)
    @Column(name = "artifact_type", nullable = false)
    private DianArtifactType artifactType;
    @Column(name = "storage_bucket_reference")
    private String storageBucketReference;
    @Column(name = "storage_key", nullable = false)
    private String storageKey;
    @Column(name = "content_type", nullable = false)
    private String contentType;
    @Column(name = "file_name", nullable = false)
    private String fileName;
    @Column(name = "content_hash", nullable = false)
    private String contentHash;
    @Column(name = "size_bytes")
    private Long sizeBytes;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "created_by")
    private UUID createdBy;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getSubmissionId() { return submissionId; }
    public void setSubmissionId(UUID submissionId) { this.submissionId = submissionId; }
    public UUID getDocumentId() { return documentId; }
    public void setDocumentId(UUID documentId) { this.documentId = documentId; }
    public DianArtifactType getArtifactType() { return artifactType; }
    public void setArtifactType(DianArtifactType artifactType) { this.artifactType = artifactType; }
    public String getStorageBucketReference() { return storageBucketReference; }
    public void setStorageBucketReference(String storageBucketReference) { this.storageBucketReference = storageBucketReference; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getContentHash() { return contentHash; }
    public void setContentHash(String contentHash) { this.contentHash = contentHash; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}

package com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportNotificationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_export_job")
public class ReportExportJobJpaEntity {

    @Id
    private UUID id;
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    @Column(name = "requested_by_user_id")
    private UUID requestedByUserId;
    @Column(name = "report_code", nullable = false, length = 80)
    private String reportCode;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportExportFormat format;
    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false, length = 20)
    private ChartType chartType;
    @Column(name = "from_date")
    private LocalDate fromDate;
    @Column(name = "to_date")
    private LocalDate toDate;
    @Column(name = "filters_json", nullable = false, columnDefinition = "TEXT")
    private String filtersJson;
    @Column(name = "notify_by_email", nullable = false)
    private boolean notifyByEmail;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportExportJobStatus status;
    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;
    @Column(name = "started_at")
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "token_hash", length = 120)
    private String tokenHash;
    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;
    @Column(name = "storage_key")
    private String storageKey;
    @Column
    private String filename;
    @Column(name = "content_type", length = 160)
    private String contentType;
    @Column(name = "file_size")
    private Long fileSize;
    @Column(name = "failure_message", length = 500)
    private String failureMessage;
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 20)
    private ReportNotificationStatus notificationStatus;
    @Column(name = "notification_message", length = 500)
    private String notificationMessage;
    @Column(name = "download_attempts", nullable = false)
    private int downloadAttempts;
    @Column(name = "last_downloaded_at")
    private Instant lastDownloadedAt;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCompanyId() { return companyId; }
    public void setCompanyId(UUID companyId) { this.companyId = companyId; }
    public UUID getRequestedByUserId() { return requestedByUserId; }
    public void setRequestedByUserId(UUID requestedByUserId) { this.requestedByUserId = requestedByUserId; }
    public String getReportCode() { return reportCode; }
    public void setReportCode(String reportCode) { this.reportCode = reportCode; }
    public ReportExportFormat getFormat() { return format; }
    public void setFormat(ReportExportFormat format) { this.format = format; }
    public ChartType getChartType() { return chartType; }
    public void setChartType(ChartType chartType) { this.chartType = chartType; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getFiltersJson() { return filtersJson; }
    public void setFiltersJson(String filtersJson) { this.filtersJson = filtersJson; }
    public boolean isNotifyByEmail() { return notifyByEmail; }
    public void setNotifyByEmail(boolean notifyByEmail) { this.notifyByEmail = notifyByEmail; }
    public ReportExportJobStatus getStatus() { return status; }
    public void setStatus(ReportExportJobStatus status) { this.status = status; }
    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public Instant getTokenExpiresAt() { return tokenExpiresAt; }
    public void setTokenExpiresAt(Instant tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getFailureMessage() { return failureMessage; }
    public void setFailureMessage(String failureMessage) { this.failureMessage = failureMessage; }
    public ReportNotificationStatus getNotificationStatus() { return notificationStatus; }
    public void setNotificationStatus(ReportNotificationStatus notificationStatus) { this.notificationStatus = notificationStatus; }
    public String getNotificationMessage() { return notificationMessage; }
    public void setNotificationMessage(String notificationMessage) { this.notificationMessage = notificationMessage; }
    public int getDownloadAttempts() { return downloadAttempts; }
    public void setDownloadAttempts(int downloadAttempts) { this.downloadAttempts = downloadAttempts; }
    public Instant getLastDownloadedAt() { return lastDownloadedAt; }
    public void setLastDownloadedAt(Instant lastDownloadedAt) { this.lastDownloadedAt = lastDownloadedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

package com.msvanegasg.facturaelectronica.reporting.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ReportExportJob {

    private final UUID id;
    private final UUID companyId;
    private final UUID requestedByUserId;
    private final String reportCode;
    private final ReportExportFormat format;
    private final ChartType chartType;
    private final LocalDate fromDate;
    private final LocalDate toDate;
    private final Map<String, String> filters;
    private final boolean notifyByEmail;
    private final ReportExportJobStatus status;
    private final Instant requestedAt;
    private final Instant startedAt;
    private final Instant completedAt;
    private final Instant expiresAt;
    private final String tokenHash;
    private final Instant tokenExpiresAt;
    private final String storageKey;
    private final String filename;
    private final String contentType;
    private final Long fileSize;
    private final String failureMessage;
    private final ReportNotificationStatus notificationStatus;
    private final String notificationMessage;
    private final int downloadAttempts;
    private final Instant lastDownloadedAt;
    private final Instant createdAt;
    private final Instant updatedAt;

    private ReportExportJob(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.companyId = Objects.requireNonNull(builder.companyId, "companyId is required");
        this.requestedByUserId = builder.requestedByUserId;
        this.reportCode = requireText(builder.reportCode, "reportCode");
        this.format = Objects.requireNonNull(builder.format, "format is required");
        this.chartType = Objects.requireNonNull(builder.chartType, "chartType is required");
        this.fromDate = builder.fromDate;
        this.toDate = builder.toDate;
        this.filters = Map.copyOf(builder.filters == null ? Map.of() : builder.filters);
        this.notifyByEmail = builder.notifyByEmail;
        this.status = Objects.requireNonNull(builder.status, "status is required");
        this.requestedAt = Objects.requireNonNull(builder.requestedAt, "requestedAt is required");
        this.startedAt = builder.startedAt;
        this.completedAt = builder.completedAt;
        this.expiresAt = Objects.requireNonNull(builder.expiresAt, "expiresAt is required");
        this.tokenHash = builder.tokenHash;
        this.tokenExpiresAt = builder.tokenExpiresAt;
        this.storageKey = builder.storageKey;
        this.filename = builder.filename;
        this.contentType = builder.contentType;
        this.fileSize = builder.fileSize;
        this.failureMessage = builder.failureMessage;
        this.notificationStatus = Objects.requireNonNull(builder.notificationStatus, "notificationStatus is required");
        this.notificationMessage = builder.notificationMessage;
        this.downloadAttempts = builder.downloadAttempts;
        this.lastDownloadedAt = builder.lastDownloadedAt;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(builder.updatedAt, "updatedAt is required");
    }

    public static ReportExportJob create(UUID id, UUID companyId, UUID requestedByUserId, String reportCode,
            ReportExportFormat format, ChartType chartType, LocalDate fromDate, LocalDate toDate,
            Map<String, String> filters, boolean notifyByEmail, Instant now, Instant expiresAt) {
        if (toDate != null && fromDate != null && toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("to must be greater than or equal to from");
        }
        return builder()
                .id(id)
                .companyId(companyId)
                .requestedByUserId(requestedByUserId)
                .reportCode(reportCode)
                .format(format == null ? ReportExportFormat.CSV : format)
                .chartType(chartType == null ? ChartType.TABLE : chartType)
                .fromDate(fromDate)
                .toDate(toDate)
                .filters(filters == null ? Map.of() : filters)
                .notifyByEmail(notifyByEmail)
                .status(ReportExportJobStatus.PENDING)
                .requestedAt(now)
                .expiresAt(expiresAt)
                .notificationStatus(notifyByEmail ? ReportNotificationStatus.PENDING : ReportNotificationStatus.NOT_REQUESTED)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public ReportExportJob processing(Instant now) {
        return copy().status(ReportExportJobStatus.PROCESSING).startedAt(now).updatedAt(now).build();
    }

    public ReportExportJob ready(String storageKey, String filename, String contentType, long fileSize, Instant now) {
        return copy()
                .status(ReportExportJobStatus.READY)
                .storageKey(requireText(storageKey, "storageKey"))
                .filename(requireText(filename, "filename"))
                .contentType(requireText(contentType, "contentType"))
                .fileSize(fileSize)
                .completedAt(now)
                .updatedAt(now)
                .build();
    }

    public ReportExportJob failed(String message, Instant now) {
        return copy()
                .status(ReportExportJobStatus.FAILED)
                .failureMessage(truncate(message, 500))
                .completedAt(now)
                .updatedAt(now)
                .build();
    }

    public ReportExportJob withDownloadToken(String tokenHash, Instant tokenExpiresAt, Instant now) {
        ensureReady();
        return copy()
                .tokenHash(requireText(tokenHash, "tokenHash"))
                .tokenExpiresAt(Objects.requireNonNull(tokenExpiresAt, "tokenExpiresAt is required"))
                .updatedAt(now)
                .build();
    }

    public ReportExportJob withNotification(ReportNotificationStatus status, String message, Instant now) {
        return copy()
                .notificationStatus(status)
                .notificationMessage(truncate(message, 500))
                .updatedAt(now)
                .build();
    }

    public ReportExportJob downloaded(Instant now) {
        ensureReady();
        return copy()
                .downloadAttempts(downloadAttempts + 1)
                .lastDownloadedAt(now)
                .updatedAt(now)
                .build();
    }

    public ReportExportJob expired(Instant now) {
        return copy()
                .status(ReportExportJobStatus.EXPIRED)
                .updatedAt(now)
                .build();
    }

    public boolean isDownloadableAt(Instant now) {
        return status == ReportExportJobStatus.READY
                && storageKey != null
                && expiresAt.isAfter(now)
                && tokenHash != null
                && tokenExpiresAt != null
                && tokenExpiresAt.isAfter(now);
    }

    private void ensureReady() {
        if (status != ReportExportJobStatus.READY) {
            throw new IllegalStateException("report export job is not ready");
        }
    }

    private Builder copy() {
        return builder()
                .id(id)
                .companyId(companyId)
                .requestedByUserId(requestedByUserId)
                .reportCode(reportCode)
                .format(format)
                .chartType(chartType)
                .fromDate(fromDate)
                .toDate(toDate)
                .filters(filters)
                .notifyByEmail(notifyByEmail)
                .status(status)
                .requestedAt(requestedAt)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .expiresAt(expiresAt)
                .tokenHash(tokenHash)
                .tokenExpiresAt(tokenExpiresAt)
                .storageKey(storageKey)
                .filename(filename)
                .contentType(contentType)
                .fileSize(fileSize)
                .failureMessage(failureMessage)
                .notificationStatus(notificationStatus)
                .notificationMessage(notificationMessage)
                .downloadAttempts(downloadAttempts)
                .lastDownloadedAt(lastDownloadedAt)
                .createdAt(createdAt)
                .updatedAt(updatedAt);
    }

    public UUID id() { return id; }
    public UUID companyId() { return companyId; }
    public UUID requestedByUserId() { return requestedByUserId; }
    public String reportCode() { return reportCode; }
    public ReportExportFormat format() { return format; }
    public ChartType chartType() { return chartType; }
    public LocalDate fromDate() { return fromDate; }
    public LocalDate toDate() { return toDate; }
    public Map<String, String> filters() { return filters; }
    public boolean notifyByEmail() { return notifyByEmail; }
    public ReportExportJobStatus status() { return status; }
    public Instant requestedAt() { return requestedAt; }
    public Instant startedAt() { return startedAt; }
    public Instant completedAt() { return completedAt; }
    public Instant expiresAt() { return expiresAt; }
    public String tokenHash() { return tokenHash; }
    public Instant tokenExpiresAt() { return tokenExpiresAt; }
    public String storageKey() { return storageKey; }
    public String filename() { return filename; }
    public String contentType() { return contentType; }
    public Long fileSize() { return fileSize; }
    public String failureMessage() { return failureMessage; }
    public ReportNotificationStatus notificationStatus() { return notificationStatus; }
    public String notificationMessage() { return notificationMessage; }
    public int downloadAttempts() { return downloadAttempts; }
    public Instant lastDownloadedAt() { return lastDownloadedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    public static final class Builder {
        private UUID id;
        private UUID companyId;
        private UUID requestedByUserId;
        private String reportCode;
        private ReportExportFormat format;
        private ChartType chartType;
        private LocalDate fromDate;
        private LocalDate toDate;
        private Map<String, String> filters = Map.of();
        private boolean notifyByEmail;
        private ReportExportJobStatus status;
        private Instant requestedAt;
        private Instant startedAt;
        private Instant completedAt;
        private Instant expiresAt;
        private String tokenHash;
        private Instant tokenExpiresAt;
        private String storageKey;
        private String filename;
        private String contentType;
        private Long fileSize;
        private String failureMessage;
        private ReportNotificationStatus notificationStatus;
        private String notificationMessage;
        private int downloadAttempts;
        private Instant lastDownloadedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder companyId(UUID companyId) { this.companyId = companyId; return this; }
        public Builder requestedByUserId(UUID requestedByUserId) { this.requestedByUserId = requestedByUserId; return this; }
        public Builder reportCode(String reportCode) { this.reportCode = reportCode; return this; }
        public Builder format(ReportExportFormat format) { this.format = format; return this; }
        public Builder chartType(ChartType chartType) { this.chartType = chartType; return this; }
        public Builder fromDate(LocalDate fromDate) { this.fromDate = fromDate; return this; }
        public Builder toDate(LocalDate toDate) { this.toDate = toDate; return this; }
        public Builder filters(Map<String, String> filters) { this.filters = filters; return this; }
        public Builder notifyByEmail(boolean notifyByEmail) { this.notifyByEmail = notifyByEmail; return this; }
        public Builder status(ReportExportJobStatus status) { this.status = status; return this; }
        public Builder requestedAt(Instant requestedAt) { this.requestedAt = requestedAt; return this; }
        public Builder startedAt(Instant startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder tokenHash(String tokenHash) { this.tokenHash = tokenHash; return this; }
        public Builder tokenExpiresAt(Instant tokenExpiresAt) { this.tokenExpiresAt = tokenExpiresAt; return this; }
        public Builder storageKey(String storageKey) { this.storageKey = storageKey; return this; }
        public Builder filename(String filename) { this.filename = filename; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder fileSize(Long fileSize) { this.fileSize = fileSize; return this; }
        public Builder failureMessage(String failureMessage) { this.failureMessage = failureMessage; return this; }
        public Builder notificationStatus(ReportNotificationStatus notificationStatus) { this.notificationStatus = notificationStatus; return this; }
        public Builder notificationMessage(String notificationMessage) { this.notificationMessage = notificationMessage; return this; }
        public Builder downloadAttempts(int downloadAttempts) { this.downloadAttempts = downloadAttempts; return this; }
        public Builder lastDownloadedAt(Instant lastDownloadedAt) { this.lastDownloadedAt = lastDownloadedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder fileSize(long fileSize) { this.fileSize = fileSize; return this; }

        public ReportExportJob build() {
            return new ReportExportJob(this);
        }
    }
}

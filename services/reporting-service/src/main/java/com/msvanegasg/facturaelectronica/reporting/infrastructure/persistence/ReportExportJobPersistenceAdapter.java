package com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportJobRepositoryPort;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJob;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.entity.ReportExportJobJpaEntity;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.persistence.repository.ReportExportJobJpaRepository;

@Repository
public class ReportExportJobPersistenceAdapter implements ReportExportJobRepositoryPort {

    private static final TypeReference<Map<String, String>> FILTERS_TYPE = new TypeReference<>() {
    };

    private final ReportExportJobJpaRepository repository;
    private final ObjectMapper objectMapper;

    public ReportExportJobPersistenceAdapter(ReportExportJobJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public ReportExportJob save(ReportExportJob job) {
        return toDomain(repository.save(toEntity(job)));
    }

    @Override
    public Optional<ReportExportJob> findByCompanyIdAndId(UUID companyId, UUID jobId) {
        return repository.findByCompanyIdAndId(companyId, jobId).map(this::toDomain);
    }

    @Override
    public Optional<ReportExportJob> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public List<ReportExportJob> findByCompany(UUID companyId, UUID requestedByUserId, ReportExportJobStatus status) {
        if (requestedByUserId != null && status != null) {
            return repository.findByCompanyIdAndRequestedByUserIdAndStatusOrderByRequestedAtDesc(companyId,
                    requestedByUserId, status).stream().map(this::toDomain).toList();
        }
        if (requestedByUserId != null) {
            return repository.findByCompanyIdAndRequestedByUserIdOrderByRequestedAtDesc(companyId, requestedByUserId)
                    .stream().map(this::toDomain).toList();
        }
        if (status != null) {
            return repository.findByCompanyIdAndStatusOrderByRequestedAtDesc(companyId, status).stream()
                    .map(this::toDomain).toList();
        }
        return repository.findByCompanyIdOrderByRequestedAtDesc(companyId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ReportExportJob> findPending(int limit) {
        return repository.findByStatusOrderByRequestedAtAsc(ReportExportJobStatus.PENDING,
                PageRequest.of(0, Math.max(1, limit))).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ReportExportJob> findReadyExpiredBefore(Instant now) {
        return repository.findByStatusAndExpiresAtBefore(ReportExportJobStatus.READY, now).stream()
                .map(this::toDomain).toList();
    }

    private ReportExportJobJpaEntity toEntity(ReportExportJob job) {
        ReportExportJobJpaEntity entity = new ReportExportJobJpaEntity();
        entity.setId(job.id());
        entity.setCompanyId(job.companyId());
        entity.setRequestedByUserId(job.requestedByUserId());
        entity.setReportCode(job.reportCode());
        entity.setFormat(job.format());
        entity.setChartType(job.chartType());
        entity.setFromDate(job.fromDate());
        entity.setToDate(job.toDate());
        entity.setFiltersJson(writeFilters(job.filters()));
        entity.setNotifyByEmail(job.notifyByEmail());
        entity.setStatus(job.status());
        entity.setRequestedAt(job.requestedAt());
        entity.setStartedAt(job.startedAt());
        entity.setCompletedAt(job.completedAt());
        entity.setExpiresAt(job.expiresAt());
        entity.setTokenHash(job.tokenHash());
        entity.setTokenExpiresAt(job.tokenExpiresAt());
        entity.setStorageKey(job.storageKey());
        entity.setFilename(job.filename());
        entity.setContentType(job.contentType());
        entity.setFileSize(job.fileSize());
        entity.setFailureMessage(job.failureMessage());
        entity.setNotificationStatus(job.notificationStatus());
        entity.setNotificationMessage(job.notificationMessage());
        entity.setDownloadAttempts(job.downloadAttempts());
        entity.setLastDownloadedAt(job.lastDownloadedAt());
        entity.setCreatedAt(job.createdAt());
        entity.setUpdatedAt(job.updatedAt());
        return entity;
    }

    private ReportExportJob toDomain(ReportExportJobJpaEntity entity) {
        return ReportExportJob.builder()
                .id(entity.getId())
                .companyId(entity.getCompanyId())
                .requestedByUserId(entity.getRequestedByUserId())
                .reportCode(entity.getReportCode())
                .format(entity.getFormat())
                .chartType(entity.getChartType())
                .fromDate(entity.getFromDate())
                .toDate(entity.getToDate())
                .filters(readFilters(entity.getFiltersJson()))
                .notifyByEmail(entity.isNotifyByEmail())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .expiresAt(entity.getExpiresAt())
                .tokenHash(entity.getTokenHash())
                .tokenExpiresAt(entity.getTokenExpiresAt())
                .storageKey(entity.getStorageKey())
                .filename(entity.getFilename())
                .contentType(entity.getContentType())
                .fileSize(entity.getFileSize())
                .failureMessage(entity.getFailureMessage())
                .notificationStatus(entity.getNotificationStatus())
                .notificationMessage(entity.getNotificationMessage())
                .downloadAttempts(entity.getDownloadAttempts())
                .lastDownloadedAt(entity.getLastDownloadedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String writeFilters(Map<String, String> filters) {
        try {
            return objectMapper.writeValueAsString(filters == null ? Map.of() : filters);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar filtros del reporte.", exception);
        }
    }

    private Map<String, String> readFilters(String filtersJson) {
        if (filtersJson == null || filtersJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(filtersJson, FILTERS_TYPE);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible leer filtros del reporte.", exception);
        }
    }
}

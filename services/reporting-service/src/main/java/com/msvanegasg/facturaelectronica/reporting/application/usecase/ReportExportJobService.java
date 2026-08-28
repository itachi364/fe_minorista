package com.msvanegasg.facturaelectronica.reporting.application.usecase;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.reporting.application.dto.CreateReportExportJobCommand;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportDownloadLinkResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportDownloadResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportJobResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryCommand;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportExportJobsUseCase;
import com.msvanegasg.facturaelectronica.reporting.application.port.in.ManageReportsUseCase;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportDownloadAttemptPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportJobRepositoryPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportStoragePort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportNotificationPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.TokenHashPort;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportCatalog;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJob;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportNotificationStatus;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.config.ReportExportProperties;

public class ReportExportJobService implements ManageReportExportJobsUseCase {

    private final ReportExportJobRepositoryPort repository;
    private final ReportExportDownloadAttemptPort downloadAttempts;
    private final ReportExportStoragePort storage;
    private final ReportNotificationPort notificationPort;
    private final TokenHashPort tokenHashPort;
    private final IdGeneratorPort idGenerator;
    private final ManageReportsUseCase reportsUseCase;
    private final ReportExportProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public ReportExportJobService(ReportExportJobRepositoryPort repository,
            ReportExportDownloadAttemptPort downloadAttempts, ReportExportStoragePort storage,
            ReportNotificationPort notificationPort, TokenHashPort tokenHashPort, IdGeneratorPort idGenerator,
            ManageReportsUseCase reportsUseCase, ReportExportProperties properties, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.downloadAttempts = Objects.requireNonNull(downloadAttempts);
        this.storage = Objects.requireNonNull(storage);
        this.notificationPort = Objects.requireNonNull(notificationPort);
        this.tokenHashPort = Objects.requireNonNull(tokenHashPort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.reportsUseCase = Objects.requireNonNull(reportsUseCase);
        this.properties = Objects.requireNonNull(properties);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ReportExportJobResult create(CreateReportExportJobCommand command) {
        Objects.requireNonNull(command, "command is required");
        requireCompany(command.companyId());
        ReportCatalog.find(command.reportCode()).orElseThrow(() -> new ReportNotFoundException(command.reportCode()));
        Instant now = clock.instant();
        ReportExportJob job = ReportExportJob.create(idGenerator.newId(), command.companyId(),
                command.requestedByUserId(), command.reportCode(), command.format(), command.chartType(), command.from(),
                command.to(), command.filters(), command.notifyByEmail(), now,
                now.plus(properties.retentionDuration()));
        return toResult(repository.save(job));
    }

    @Override
    public List<ReportExportJobResult> find(UUID companyId, UUID requestedByUserId, ReportExportJobStatus status) {
        requireCompany(companyId);
        expireReadyJobs();
        return repository.findByCompany(companyId, requestedByUserId, status).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public ReportExportJobResult findById(UUID companyId, UUID jobId) {
        return toResult(findJob(companyId, jobId));
    }

    @Override
    public ReportDownloadLinkResult createDownloadLink(UUID companyId, UUID jobId) {
        DownloadLink downloadLink = createDownloadLinkFor(findJob(companyId, jobId));
        return new ReportDownloadLinkResult(downloadLink.job().id(), downloadLink.link(), downloadLink.job().tokenExpiresAt(),
                properties.downloadPresignedTtlSeconds());
    }

    @Override
    public ReportDownloadResult downloadByToken(String token) {
        String tokenHash = tokenHashPort.hash(token);
        ReportExportJob job = repository.findByTokenHash(tokenHash)
                .orElseThrow(ReportExportJobNotFoundException::new);
        Instant now = clock.instant();
        if (!job.isDownloadableAt(now)) {
            downloadAttempts.record(idGenerator.newId(), job.id(), job.companyId(), now, "DENIED",
                    "token_expired_or_unavailable");
            throw new IllegalStateException("El enlace de descarga vencio o el reporte ya no esta disponible.");
        }
        try {
            byte[] content = storage.read(job.storageKey());
            repository.save(job.downloaded(now));
            downloadAttempts.record(idGenerator.newId(), job.id(), job.companyId(), now, "SUCCESS", "downloaded");
            return new ReportDownloadResult(job.filename(), job.contentType(), content,
                    properties.downloadPresignedTtlSeconds());
        } catch (RuntimeException exception) {
            downloadAttempts.record(idGenerator.newId(), job.id(), job.companyId(), now, "FAILED",
                    exception.getMessage());
            throw exception;
        }
    }

    @Override
    public int processPending() {
        expireReadyJobs();
        List<ReportExportJob> pending = repository.findPending(properties.batchSize());
        pending.forEach(this::process);
        return pending.size();
    }

    private void process(ReportExportJob job) {
        ReportExportJob processing = repository.save(job.processing(clock.instant()));
        try {
            var export = reportsUseCase.export(new ReportQueryCommand(processing.companyId(), processing.reportCode(),
                    processing.fromDate(), processing.toDate(), processing.filters(), processing.chartType(), null),
                    processing.format());
            var stored = storage.store(processing.companyId(), processing.id(), export);
            ReportExportJob ready = repository.save(processing.ready(stored.storageKey(), stored.filename(),
                    stored.contentType(), stored.fileSize(), clock.instant()));
            if (ready.notifyByEmail()) {
                notifyReady(ready);
            }
        } catch (RuntimeException exception) {
            repository.save(processing.failed(exception.getMessage(), clock.instant()));
        }
    }

    private void notifyReady(ReportExportJob job) {
        try {
            DownloadLink downloadLink = createDownloadLinkFor(job);
            notificationPort.notifyReady(downloadLink.job(), downloadLink.link());
            repository.save(downloadLink.job().withNotification(ReportNotificationStatus.SENT, "Notificacion registrada.",
                    clock.instant()));
        } catch (RuntimeException exception) {
            repository.save(job.withNotification(ReportNotificationStatus.FAILED, exception.getMessage(),
                    clock.instant()));
        }
    }

    private void expireReadyJobs() {
        Instant now = clock.instant();
        repository.findReadyExpiredBefore(now).forEach(job -> repository.save(job.expired(now)));
    }

    private ReportExportJob findJob(UUID companyId, UUID jobId) {
        requireCompany(companyId);
        Objects.requireNonNull(jobId, "jobId is required");
        return repository.findByCompanyIdAndId(companyId, jobId)
                .orElseThrow(() -> new ReportExportJobNotFoundException(jobId));
    }

    private DownloadLink createDownloadLinkFor(ReportExportJob job) {
        if (job.status() != ReportExportJobStatus.READY) {
            throw new IllegalStateException("El reporte todavia no esta listo para descargar.");
        }
        Instant now = clock.instant();
        String token = newToken();
        ReportExportJob saved = repository.save(job.withDownloadToken(tokenHashPort.hash(token),
                now.plus(properties.linkTokenTtl()), now));
        String link = properties.appPublicBaseUrl().replaceAll("/+$", "") + "/reportes/descarga/" + token;
        return new DownloadLink(saved, link);
    }

    private ReportExportJobResult toResult(ReportExportJob job) {
        return new ReportExportJobResult(job.id(), job.companyId(), job.requestedByUserId(), job.reportCode(),
                job.format(), job.chartType(), job.fromDate(), job.toDate(), Map.copyOf(job.filters()),
                job.notifyByEmail(), job.status(), job.isDownloadableAt(clock.instant()), job.filename(),
                job.contentType(), job.fileSize(), job.requestedAt(), job.startedAt(), job.completedAt(),
                job.expiresAt(), job.tokenExpiresAt(), job.notificationStatus(), job.notificationMessage(),
                job.failureMessage(), job.downloadAttempts(), job.lastDownloadedAt());
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record DownloadLink(ReportExportJob job, String link) {
    }

    private static void requireCompany(UUID companyId) {
        if (companyId == null) {
            throw new IllegalArgumentException("companyId is required");
        }
    }
}

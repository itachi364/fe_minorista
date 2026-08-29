package com.msvanegasg.facturaelectronica.reporting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.reporting.application.dto.CreateReportExportJobCommand;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportExportResult;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOption;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryCommand;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportDataGateway;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportDownloadAttemptPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportJobRepositoryPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportExportStoragePort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportNotificationPort;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.TokenHashPort;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJob;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportJobStatus;
import com.msvanegasg.facturaelectronica.reporting.infrastructure.config.ReportExportProperties;

class ReportExportJobServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID JOB_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-27T10:00:00Z"), ZoneOffset.UTC);

    private final InMemoryJobRepository repository = new InMemoryJobRepository();
    private final InMemoryStorage storage = new InMemoryStorage();
    private final FakeDownloadAttempts downloadAttempts = new FakeDownloadAttempts();
    private final FakeNotificationPort notifications = new FakeNotificationPort();
    private final ReportManagementService reports = new ReportManagementService(new FakeReportDataGateway());
    private final ReportExportJobService service = new ReportExportJobService(repository, downloadAttempts, storage, notifications,
            token -> "hash:" + token, () -> JOB_ID, reports,
            new ReportExportProperties("http://localhost:5173", "target/report-exports-test", 5, 72, 7, true,
                    5000, 5),
            CLOCK);

    @Test
    void createsPendingJobAndProcessesItAsReady() {
        var created = service.create(command(false));

        assertThat(created.jobId()).isEqualTo(JOB_ID);
        assertThat(created.status()).isEqualTo(ReportExportJobStatus.PENDING);

        int processed = service.processPending();

        var ready = service.findById(COMPANY_ID, JOB_ID);
        assertThat(processed).isEqualTo(1);
        assertThat(ready.status()).isEqualTo(ReportExportJobStatus.READY);
        assertThat(ready.filename()).endsWith(".xls");
        assertThat(ready.downloadAvailable()).isFalse();
    }

    @Test
    void createsTemporaryDownloadLinkAndDownloadsByToken() {
        service.create(command(false));
        service.processPending();

        var link = service.createDownloadLink(COMPANY_ID, JOB_ID);

        assertThat(link.downloadLink()).startsWith("http://localhost:5173/reportes/descarga/");
        assertThat(link.presignedTtlSeconds()).isEqualTo(5);

        String token = link.downloadLink().substring(link.downloadLink().lastIndexOf('/') + 1);
        var download = service.downloadByToken(token);

        assertThat(download.filename()).endsWith(".xls");
        assertThat(new String(download.content(), StandardCharsets.UTF_8)).contains("Cafe");
        assertThat(service.findById(COMPANY_ID, JOB_ID).downloadAttempts()).isEqualTo(1);
        assertThat(downloadAttempts.results).containsExactly("SUCCESS");
    }

    @Test
    void sendsNotificationWithoutDroppingDownloadToken() {
        service.create(command(true));
        service.processPending();

        var job = service.findById(COMPANY_ID, JOB_ID);

        assertThat(notifications.sent).isEqualTo(1);
        assertThat(job.notificationMessage()).isEqualTo("Notificacion registrada.");
        assertThat(job.downloadAvailable()).isTrue();
    }

    @Test
    void rejectsDownloadWhenJobIsNotReady() {
        service.create(command(false));

        assertThatThrownBy(() -> service.createDownloadLink(COMPANY_ID, JOB_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("todavia no esta listo");
    }

    private static CreateReportExportJobCommand command(boolean notifyByEmail) {
        return new CreateReportExportJobCommand(COMPANY_ID, USER_ID, "SALES_BY_PRODUCT", ReportExportFormat.XLS,
                ChartType.TABLE, LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-27"), Map.of(),
                notifyByEmail, "Bearer token");
    }

    private static final class InMemoryJobRepository implements ReportExportJobRepositoryPort {
        private final List<ReportExportJob> jobs = new ArrayList<>();

        @Override
        public ReportExportJob save(ReportExportJob job) {
            jobs.removeIf(existing -> existing.id().equals(job.id()));
            jobs.add(job);
            return job;
        }

        @Override
        public Optional<ReportExportJob> findByCompanyIdAndId(UUID companyId, UUID jobId) {
            return jobs.stream().filter(job -> job.companyId().equals(companyId) && job.id().equals(jobId))
                    .findFirst();
        }

        @Override
        public Optional<ReportExportJob> findByTokenHash(String tokenHash) {
            return jobs.stream().filter(job -> tokenHash.equals(job.tokenHash())).findFirst();
        }

        @Override
        public List<ReportExportJob> findByCompany(UUID companyId, UUID requestedByUserId,
                ReportExportJobStatus status) {
            return jobs.stream()
                    .filter(job -> job.companyId().equals(companyId))
                    .filter(job -> requestedByUserId == null || requestedByUserId.equals(job.requestedByUserId()))
                    .filter(job -> status == null || status == job.status())
                    .toList();
        }

        @Override
        public List<ReportExportJob> findPending(int limit) {
            return jobs.stream().filter(job -> job.status() == ReportExportJobStatus.PENDING).limit(limit).toList();
        }

        @Override
        public List<ReportExportJob> findReadyExpiredBefore(Instant now) {
            return jobs.stream().filter(job -> job.status() == ReportExportJobStatus.READY
                    && job.expiresAt().isBefore(now)).toList();
        }
    }

    private static final class InMemoryStorage implements ReportExportStoragePort {
        private byte[] content;

        @Override
        public StoredReport store(UUID companyId, UUID jobId, ReportExportResult export) {
            this.content = export.content();
            return new StoredReport(companyId + "/" + jobId + "/" + export.filename(), export.filename(),
                    export.contentType(), export.content().length);
        }

        @Override
        public byte[] read(String storageKey) {
            return content;
        }
    }

    private static final class FakeNotificationPort implements ReportNotificationPort {
        private int sent;

        @Override
        public void notifyReady(ReportExportJob job, String downloadLink) {
            sent++;
        }
    }

    private static final class FakeDownloadAttempts implements ReportExportDownloadAttemptPort {
        private final List<String> results = new ArrayList<>();

        @Override
        public void record(UUID id, UUID jobId, UUID companyId, Instant attemptedAt, String result, String detail) {
            results.add(result);
        }
    }

    private static final class FakeReportDataGateway implements ReportDataGateway {

        @Override
        public JsonNode fetchReport(UUID companyId, String reportCode, LocalDate from, LocalDate to,
                Map<String, String> filters, String authorizationHeader) {
            var mapper = new ObjectMapper();
            var sale = mapper.createObjectNode();
            sale.put("id", "sale-1");
            sale.put("status", "CONFIRMED");
            sale.put("createdBy", USER_ID.toString());
            sale.put("subtotal", "15000.00");
            sale.put("taxTotal", "2850.00");
            sale.put("total", "17850.00");
            var line = mapper.createObjectNode();
            line.put("productId", "product-1");
            line.put("productName", "Cafe");
            line.put("quantity", 1);
            line.put("subtotal", "15000.00");
            line.put("taxAmount", "2850.00");
            line.put("total", "17850.00");
            sale.set("lines", mapper.createArrayNode().add(line));
            return mapper.createArrayNode().add(sale);
        }

        @Override
        public List<ReportOption> fetchOptions(UUID companyId, String optionSource, String authorizationHeader) {
            return List.of();
        }
    }
}

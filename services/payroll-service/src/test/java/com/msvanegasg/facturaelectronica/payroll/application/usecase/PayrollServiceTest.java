package com.msvanegasg.facturaelectronica.payroll.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.payroll.application.dto.DailyLaborPaymentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.ElectronicPayrollDocumentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.PayrollSettingsCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.WorkerCommand;
import com.msvanegasg.facturaelectronica.payroll.application.port.out.PayrollAccountingPort;
import com.msvanegasg.facturaelectronica.payroll.application.port.out.PayrollRepositoryPort;
import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;
import com.msvanegasg.facturaelectronica.payroll.domain.model.ElectronicPayrollDocument;
import com.msvanegasg.facturaelectronica.payroll.domain.model.PayrollSettings;
import com.msvanegasg.facturaelectronica.payroll.domain.model.Worker;

class PayrollServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID WORKER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID PAYMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID DOCUMENT_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-08-11T10:00:00Z");

    private final InMemoryPayrollRepository repository = new InMemoryPayrollRepository();
    private final CapturingPayrollAccountingPort accountingPort = new CapturingPayrollAccountingPort();
    private final PayrollService service = new PayrollService(repository, new SequenceIdGenerator(), () -> NOW,
            accountingPort);

    @Test
    void returnsDefaultSettingsWithElectronicPayrollDisabled() {
        PayrollSettings settings = service.settings(COMPANY_ID);

        assertThat(settings.electronicPayrollEnabled()).isFalse();
        assertThat(settings.providerMode()).isEqualTo("MOCK");
    }

    @Test
    void enablesElectronicPayrollByCompany() {
        PayrollSettings settings = service.configureSettings(COMPANY_ID, new PayrollSettingsCommand(true, "mock"));

        assertThat(settings.electronicPayrollEnabled()).isTrue();
        assertThat(settings.providerMode()).isEqualTo("MOCK");
    }

    @Test
    void registersDailyVerbalPaymentWhenLegalNoticeIsAccepted() {
        Worker worker = service.registerWorker(COMPANY_ID, new WorkerCommand(13, "1234567890", null,
                "Trabajador Diario", "DAILY_VERBAL", true));

        DailyLaborPayment payment = service.registerDailyPayment(COMPANY_ID, new DailyLaborPaymentCommand(worker.id(),
                LocalDate.parse("2026-08-11"), "Apoyo en ventas", new BigDecimal("80000.00"),
                new BigDecimal("80000.00"), "CASH", true, "Pago al finalizar el dia"));

        assertThat(payment.workerId()).isEqualTo(worker.id());
        assertThat(payment.legalNoticeAccepted()).isTrue();
        assertThat(accountingPort.payments).containsExactly(payment);
    }

    @Test
    void keepsDailyPaymentRegisteredWhenAccountingFails() {
        PayrollService serviceWithFailingAccounting = new PayrollService(repository, new SequenceIdGenerator(), () -> NOW,
                payment -> {
                    throw new IllegalStateException("accounting unavailable");
                });
        Worker worker = serviceWithFailingAccounting.registerWorker(COMPANY_ID, new WorkerCommand(13, "1234567890",
                null, "Trabajador Diario", "DAILY_VERBAL", true));

        DailyLaborPayment payment = serviceWithFailingAccounting.registerDailyPayment(COMPANY_ID,
                new DailyLaborPaymentCommand(worker.id(), LocalDate.parse("2026-08-11"), "Apoyo en ventas",
                        new BigDecimal("80000.00"), new BigDecimal("80000.00"), "CASH", true, null));

        assertThat(payment.id()).isEqualTo(PAYMENT_ID);
        assertThat(repository.findDailyPayment(COMPANY_ID, PAYMENT_ID)).contains(payment);
    }

    @Test
    void rejectsDailyVerbalPaymentWithoutLegalNoticeAcceptance() {
        Worker worker = service.registerWorker(COMPANY_ID, new WorkerCommand(13, "1234567890", null,
                "Trabajador Diario", "DAILY_VERBAL", true));

        assertThatThrownBy(() -> service.registerDailyPayment(COMPANY_ID, new DailyLaborPaymentCommand(worker.id(),
                LocalDate.parse("2026-08-11"), "Apoyo en ventas", new BigDecimal("80000.00"),
                new BigDecimal("80000.00"), "CASH", false, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("legalNoticeAccepted");
    }

    @Test
    void issuesMockElectronicPayrollOnlyWhenEnabled() {
        service.configureSettings(COMPANY_ID, new PayrollSettingsCommand(true, "MOCK"));
        Worker worker = service.registerWorker(COMPANY_ID, new WorkerCommand(13, "1234567890", null,
                "Trabajador Diario", "DAILY_VERBAL", true));
        DailyLaborPayment payment = service.registerDailyPayment(COMPANY_ID, new DailyLaborPaymentCommand(worker.id(),
                LocalDate.parse("2026-08-11"), "Apoyo en ventas", new BigDecimal("80000.00"),
                new BigDecimal("80000.00"), "CASH", true, null));

        ElectronicPayrollDocument document = service.issueElectronicDocument(COMPANY_ID,
                new ElectronicPayrollDocumentCommand(payment.id()));

        assertThat(document.cune()).startsWith("MOCK-CUNE-");
        assertThat(document.status()).isEqualTo("ACCEPTED");
    }

    private static final class SequenceIdGenerator implements com.msvanegasg.facturaelectronica.payroll.application.port.out.IdGeneratorPort {
        private int index;

        @Override
        public UUID newId() {
            return switch (index++) {
                case 0 -> WORKER_ID;
                case 1 -> PAYMENT_ID;
                default -> DOCUMENT_ID;
            };
        }
    }

    private static final class InMemoryPayrollRepository implements PayrollRepositoryPort {
        private PayrollSettings settings;
        private final List<Worker> workers = new ArrayList<>();
        private final List<DailyLaborPayment> payments = new ArrayList<>();
        private final List<ElectronicPayrollDocument> documents = new ArrayList<>();

        @Override
        public PayrollSettings saveSettings(PayrollSettings settings) {
            this.settings = settings;
            return settings;
        }

        @Override
        public Optional<PayrollSettings> findSettings(UUID companyId) {
            return Optional.ofNullable(settings).filter(value -> value.companyId().equals(companyId));
        }

        @Override
        public Worker saveWorker(Worker worker) {
            workers.add(worker);
            return worker;
        }

        @Override
        public List<Worker> findWorkers(UUID companyId) {
            return workers.stream().filter(worker -> worker.companyId().equals(companyId)).toList();
        }

        @Override
        public Optional<Worker> findWorker(UUID companyId, UUID workerId) {
            return workers.stream()
                    .filter(worker -> worker.companyId().equals(companyId) && worker.id().equals(workerId))
                    .findFirst();
        }

        @Override
        public DailyLaborPayment saveDailyPayment(DailyLaborPayment payment) {
            payments.add(payment);
            return payment;
        }

        @Override
        public Optional<DailyLaborPayment> findDailyPayment(UUID companyId, UUID paymentId) {
            return payments.stream()
                    .filter(payment -> payment.companyId().equals(companyId) && payment.id().equals(paymentId))
                    .findFirst();
        }

        @Override
        public List<DailyLaborPayment> findDailyPayments(UUID companyId, LocalDate from, LocalDate to) {
            return payments.stream().filter(payment -> payment.companyId().equals(companyId)).toList();
        }

        @Override
        public ElectronicPayrollDocument saveElectronicDocument(ElectronicPayrollDocument document) {
            documents.add(document);
            return document;
        }

        @Override
        public List<ElectronicPayrollDocument> findElectronicDocuments(UUID companyId) {
            return documents.stream().filter(document -> document.companyId().equals(companyId)).toList();
        }
    }

    private static final class CapturingPayrollAccountingPort implements PayrollAccountingPort {
        private final List<DailyLaborPayment> payments = new ArrayList<>();

        @Override
        public void applyDailyPayment(DailyLaborPayment payment) {
            payments.add(payment);
        }
    }
}

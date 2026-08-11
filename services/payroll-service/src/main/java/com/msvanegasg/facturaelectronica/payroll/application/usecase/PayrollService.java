package com.msvanegasg.facturaelectronica.payroll.application.usecase;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.payroll.application.dto.DailyLaborPaymentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.ElectronicPayrollDocumentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.PayrollSettingsCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.WorkerCommand;
import com.msvanegasg.facturaelectronica.payroll.application.port.in.PayrollUseCase;
import com.msvanegasg.facturaelectronica.payroll.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.payroll.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.payroll.application.port.out.PayrollRepositoryPort;
import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;
import com.msvanegasg.facturaelectronica.payroll.domain.model.ElectronicPayrollDocument;
import com.msvanegasg.facturaelectronica.payroll.domain.model.PayrollSettings;
import com.msvanegasg.facturaelectronica.payroll.domain.model.Worker;

@Service
public class PayrollService implements PayrollUseCase {

    private final PayrollRepositoryPort repository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public PayrollService(PayrollRepositoryPort repository, IdGeneratorPort idGenerator, ClockPort clock) {
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PayrollSettings settings(UUID companyId) {
        Objects.requireNonNull(companyId, "companyId is required");
        return repository.findSettings(companyId)
                .orElseGet(() -> new PayrollSettings(companyId, false, "MOCK", clock.now()));
    }

    @Override
    public PayrollSettings configureSettings(UUID companyId, PayrollSettingsCommand command) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(command, "command is required");
        return repository.saveSettings(new PayrollSettings(companyId, command.electronicPayrollEnabled(),
                command.providerMode(), clock.now()));
    }

    @Override
    public Worker registerWorker(UUID companyId, WorkerCommand command) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(command, "command is required");
        return repository.saveWorker(new Worker(idGenerator.newId(), companyId, command.identificationTypeCode(),
                command.identificationNumber(), command.verificationDigit(), command.fullName(),
                command.workerClassification(), command.active() == null || command.active(), clock.now()));
    }

    @Override
    public List<Worker> workers(UUID companyId) {
        Objects.requireNonNull(companyId, "companyId is required");
        return repository.findWorkers(companyId);
    }

    @Override
    public DailyLaborPayment registerDailyPayment(UUID companyId, DailyLaborPaymentCommand command) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(command, "command is required");
        repository.findWorker(companyId, command.workerId())
                .orElseThrow(() -> new IllegalArgumentException("workerId does not belong to company"));
        return repository.saveDailyPayment(new DailyLaborPayment(idGenerator.newId(), companyId, command.workerId(),
                command.workDate(), command.activityDescription(), command.agreedAmount(), command.paidAmount(),
                command.paymentMethodCode(), command.legalNoticeAccepted(), command.notes(), clock.now()));
    }

    @Override
    public List<DailyLaborPayment> dailyPayments(UUID companyId, LocalDate from, LocalDate to) {
        Objects.requireNonNull(companyId, "companyId is required");
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        return repository.findDailyPayments(companyId, from, to);
    }

    @Override
    public ElectronicPayrollDocument issueElectronicDocument(UUID companyId, ElectronicPayrollDocumentCommand command) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(command, "command is required");
        PayrollSettings settings = settings(companyId);
        if (!settings.electronicPayrollEnabled()) {
            throw new IllegalStateException("electronic payroll is disabled for company");
        }
        DailyLaborPayment payment = repository.findDailyPayment(companyId, command.dailyLaborPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("dailyLaborPaymentId does not belong to company"));
        String cune = "MOCK-CUNE-" + companyId.toString().substring(0, 8) + "-" + payment.id().toString().substring(0, 8);
        return repository.saveElectronicDocument(new ElectronicPayrollDocument(idGenerator.newId(), companyId,
                payment.id(), cune, "ACCEPTED", "Mock DIAN payroll provider accepted document", clock.now()));
    }

    @Override
    public List<ElectronicPayrollDocument> electronicDocuments(UUID companyId) {
        Objects.requireNonNull(companyId, "companyId is required");
        return repository.findElectronicDocuments(companyId);
    }
}

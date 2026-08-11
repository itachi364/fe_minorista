package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.payroll.application.port.out.PayrollRepositoryPort;
import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;
import com.msvanegasg.facturaelectronica.payroll.domain.model.ElectronicPayrollDocument;
import com.msvanegasg.facturaelectronica.payroll.domain.model.PayrollSettings;
import com.msvanegasg.facturaelectronica.payroll.domain.model.Worker;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.DailyLaborPaymentJpaEntity;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.ElectronicPayrollDocumentJpaEntity;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.PayrollSettingsJpaEntity;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.entity.WorkerJpaEntity;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository.DailyLaborPaymentJpaRepository;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository.ElectronicPayrollDocumentJpaRepository;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository.PayrollSettingsJpaRepository;
import com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence.repository.WorkerJpaRepository;

@Component
public class PayrollPersistenceAdapter implements PayrollRepositoryPort {

    private final PayrollSettingsJpaRepository settingsRepository;
    private final WorkerJpaRepository workerRepository;
    private final DailyLaborPaymentJpaRepository dailyPaymentRepository;
    private final ElectronicPayrollDocumentJpaRepository electronicDocumentRepository;

    public PayrollPersistenceAdapter(PayrollSettingsJpaRepository settingsRepository, WorkerJpaRepository workerRepository,
            DailyLaborPaymentJpaRepository dailyPaymentRepository,
            ElectronicPayrollDocumentJpaRepository electronicDocumentRepository) {
        this.settingsRepository = settingsRepository;
        this.workerRepository = workerRepository;
        this.dailyPaymentRepository = dailyPaymentRepository;
        this.electronicDocumentRepository = electronicDocumentRepository;
    }

    @Override
    public PayrollSettings saveSettings(PayrollSettings settings) {
        return toDomain(settingsRepository.save(toEntity(settings)));
    }

    @Override
    public Optional<PayrollSettings> findSettings(UUID companyId) {
        return settingsRepository.findById(companyId).map(PayrollPersistenceAdapter::toDomain);
    }

    @Override
    public Worker saveWorker(Worker worker) {
        return toDomain(workerRepository.save(toEntity(worker)));
    }

    @Override
    public List<Worker> findWorkers(UUID companyId) {
        return workerRepository.findByCompanyIdOrderByFullName(companyId).stream()
                .map(PayrollPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Worker> findWorker(UUID companyId, UUID workerId) {
        return workerRepository.findByCompanyIdAndId(companyId, workerId).map(PayrollPersistenceAdapter::toDomain);
    }

    @Override
    public DailyLaborPayment saveDailyPayment(DailyLaborPayment payment) {
        return toDomain(dailyPaymentRepository.save(toEntity(payment)));
    }

    @Override
    public Optional<DailyLaborPayment> findDailyPayment(UUID companyId, UUID paymentId) {
        return dailyPaymentRepository.findByCompanyIdAndId(companyId, paymentId).map(PayrollPersistenceAdapter::toDomain);
    }

    @Override
    public List<DailyLaborPayment> findDailyPayments(UUID companyId, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return dailyPaymentRepository.findByCompanyIdAndWorkDateBetweenOrderByWorkDateDesc(companyId, from, to)
                    .stream().map(PayrollPersistenceAdapter::toDomain).toList();
        }
        return dailyPaymentRepository.findByCompanyIdOrderByWorkDateDesc(companyId).stream()
                .map(PayrollPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public ElectronicPayrollDocument saveElectronicDocument(ElectronicPayrollDocument document) {
        return toDomain(electronicDocumentRepository.save(toEntity(document)));
    }

    @Override
    public List<ElectronicPayrollDocument> findElectronicDocuments(UUID companyId) {
        return electronicDocumentRepository.findByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(PayrollPersistenceAdapter::toDomain)
                .toList();
    }

    private static PayrollSettingsJpaEntity toEntity(PayrollSettings settings) {
        PayrollSettingsJpaEntity entity = new PayrollSettingsJpaEntity();
        entity.setCompanyId(settings.companyId());
        entity.setElectronicPayrollEnabled(settings.electronicPayrollEnabled());
        entity.setProviderMode(settings.providerMode());
        entity.setUpdatedAt(settings.updatedAt());
        return entity;
    }

    private static PayrollSettings toDomain(PayrollSettingsJpaEntity entity) {
        return new PayrollSettings(entity.getCompanyId(), entity.isElectronicPayrollEnabled(), entity.getProviderMode(),
                entity.getUpdatedAt());
    }

    private static WorkerJpaEntity toEntity(Worker worker) {
        WorkerJpaEntity entity = new WorkerJpaEntity();
        entity.setId(worker.id());
        entity.setCompanyId(worker.companyId());
        entity.setIdentificationTypeCode((short) worker.identificationTypeCode());
        entity.setIdentificationNumber(worker.identificationNumber());
        entity.setVerificationDigit(worker.verificationDigit() == null ? null : worker.verificationDigit().shortValue());
        entity.setFullName(worker.fullName());
        entity.setWorkerClassification(worker.workerClassification());
        entity.setActive(worker.active());
        entity.setCreatedAt(worker.createdAt());
        return entity;
    }

    private static Worker toDomain(WorkerJpaEntity entity) {
        return new Worker(entity.getId(), entity.getCompanyId(), entity.getIdentificationTypeCode(),
                entity.getIdentificationNumber(), entity.getVerificationDigit() == null ? null : entity.getVerificationDigit().intValue(), entity.getFullName(),
                entity.getWorkerClassification(), entity.isActive(), entity.getCreatedAt());
    }

    private static DailyLaborPaymentJpaEntity toEntity(DailyLaborPayment payment) {
        DailyLaborPaymentJpaEntity entity = new DailyLaborPaymentJpaEntity();
        entity.setId(payment.id());
        entity.setCompanyId(payment.companyId());
        entity.setWorkerId(payment.workerId());
        entity.setWorkDate(payment.workDate());
        entity.setActivityDescription(payment.activityDescription());
        entity.setAgreedAmount(payment.agreedAmount());
        entity.setPaidAmount(payment.paidAmount());
        entity.setPaymentMethodCode(payment.paymentMethodCode());
        entity.setLegalNoticeAccepted(payment.legalNoticeAccepted());
        entity.setNotes(payment.notes());
        entity.setCreatedAt(payment.createdAt());
        return entity;
    }

    private static DailyLaborPayment toDomain(DailyLaborPaymentJpaEntity entity) {
        return new DailyLaborPayment(entity.getId(), entity.getCompanyId(), entity.getWorkerId(), entity.getWorkDate(),
                entity.getActivityDescription(), entity.getAgreedAmount(), entity.getPaidAmount(),
                entity.getPaymentMethodCode(), entity.isLegalNoticeAccepted(), entity.getNotes(), entity.getCreatedAt());
    }

    private static ElectronicPayrollDocumentJpaEntity toEntity(ElectronicPayrollDocument document) {
        ElectronicPayrollDocumentJpaEntity entity = new ElectronicPayrollDocumentJpaEntity();
        entity.setId(document.id());
        entity.setCompanyId(document.companyId());
        entity.setDailyLaborPaymentId(document.dailyLaborPaymentId());
        entity.setCune(document.cune());
        entity.setStatus(document.status());
        entity.setProviderResponse(document.providerResponse());
        entity.setCreatedAt(document.createdAt());
        return entity;
    }

    private static ElectronicPayrollDocument toDomain(ElectronicPayrollDocumentJpaEntity entity) {
        return new ElectronicPayrollDocument(entity.getId(), entity.getCompanyId(), entity.getDailyLaborPaymentId(),
                entity.getCune(), entity.getStatus(), entity.getProviderResponse(), entity.getCreatedAt());
    }
}

package com.msvanegasg.facturaelectronica.payroll.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;
import com.msvanegasg.facturaelectronica.payroll.domain.model.ElectronicPayrollDocument;
import com.msvanegasg.facturaelectronica.payroll.domain.model.PayrollSettings;
import com.msvanegasg.facturaelectronica.payroll.domain.model.Worker;

public interface PayrollRepositoryPort {
    PayrollSettings saveSettings(PayrollSettings settings);
    Optional<PayrollSettings> findSettings(UUID companyId);
    Worker saveWorker(Worker worker);
    List<Worker> findWorkers(UUID companyId);
    Optional<Worker> findWorker(UUID companyId, UUID workerId);
    DailyLaborPayment saveDailyPayment(DailyLaborPayment payment);
    Optional<DailyLaborPayment> findDailyPayment(UUID companyId, UUID paymentId);
    List<DailyLaborPayment> findDailyPayments(UUID companyId, LocalDate from, LocalDate to);
    ElectronicPayrollDocument saveElectronicDocument(ElectronicPayrollDocument document);
    List<ElectronicPayrollDocument> findElectronicDocuments(UUID companyId);
}

package com.msvanegasg.facturaelectronica.payroll.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.payroll.application.dto.DailyLaborPaymentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.ElectronicPayrollDocumentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.PayrollSettingsCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.WorkerCommand;
import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;
import com.msvanegasg.facturaelectronica.payroll.domain.model.ElectronicPayrollDocument;
import com.msvanegasg.facturaelectronica.payroll.domain.model.PayrollSettings;
import com.msvanegasg.facturaelectronica.payroll.domain.model.Worker;

public interface PayrollUseCase {
    PayrollSettings settings(UUID companyId);
    PayrollSettings configureSettings(UUID companyId, PayrollSettingsCommand command);
    Worker registerWorker(UUID companyId, WorkerCommand command);
    List<Worker> workers(UUID companyId);
    DailyLaborPayment registerDailyPayment(UUID companyId, DailyLaborPaymentCommand command);
    List<DailyLaborPayment> dailyPayments(UUID companyId, LocalDate from, LocalDate to);
    ElectronicPayrollDocument issueElectronicDocument(UUID companyId, ElectronicPayrollDocumentCommand command);
    List<ElectronicPayrollDocument> electronicDocuments(UUID companyId);
}

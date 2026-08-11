package com.msvanegasg.facturaelectronica.payroll.interfaces.rest;

import com.msvanegasg.facturaelectronica.payroll.application.dto.DailyLaborPaymentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.ElectronicPayrollDocumentCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.PayrollSettingsCommand;
import com.msvanegasg.facturaelectronica.payroll.application.dto.WorkerCommand;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.DailyLaborPaymentRequest;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.ElectronicPayrollDocumentRequest;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.PayrollSettingsRequest;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.WorkerRequest;

final class PayrollRestMapper {
    private PayrollRestMapper() {
    }

    static PayrollSettingsCommand toCommand(PayrollSettingsRequest request) {
        return new PayrollSettingsCommand(request.electronicPayrollEnabled(), request.providerMode());
    }

    static WorkerCommand toCommand(WorkerRequest request) {
        return new WorkerCommand(request.identificationTypeCode(), request.identificationNumber(),
                request.verificationDigit(), request.fullName(), request.workerClassification(), request.active());
    }

    static DailyLaborPaymentCommand toCommand(DailyLaborPaymentRequest request) {
        return new DailyLaborPaymentCommand(request.workerId(), request.workDate(), request.activityDescription(),
                request.agreedAmount(), request.paidAmount(), request.paymentMethodCode(),
                request.legalNoticeAccepted(), request.notes());
    }

    static ElectronicPayrollDocumentCommand toCommand(ElectronicPayrollDocumentRequest request) {
        return new ElectronicPayrollDocumentCommand(request.dailyLaborPaymentId());
    }
}

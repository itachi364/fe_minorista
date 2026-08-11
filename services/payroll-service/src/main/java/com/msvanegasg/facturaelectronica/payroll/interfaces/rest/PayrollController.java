package com.msvanegasg.facturaelectronica.payroll.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.payroll.application.port.in.PayrollUseCase;
import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;
import com.msvanegasg.facturaelectronica.payroll.domain.model.ElectronicPayrollDocument;
import com.msvanegasg.facturaelectronica.payroll.domain.model.PayrollSettings;
import com.msvanegasg.facturaelectronica.payroll.domain.model.Worker;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.DailyLaborPaymentRequest;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.ElectronicPayrollDocumentRequest;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.PayrollSettingsRequest;
import com.msvanegasg.facturaelectronica.payroll.interfaces.rest.dto.WorkerRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payroll")
public class PayrollController {

    private static final String COMPANY_HEADER = "X-Company-Id";

    private final PayrollUseCase payrollUseCase;

    public PayrollController(PayrollUseCase payrollUseCase) {
        this.payrollUseCase = payrollUseCase;
    }

    @GetMapping("/settings")
    public ResponseEntity<PayrollSettings> settings(@RequestHeader(COMPANY_HEADER) UUID companyId) {
        return ResponseEntity.ok(payrollUseCase.settings(companyId));
    }

    @PutMapping("/settings")
    public ResponseEntity<PayrollSettings> configureSettings(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestBody PayrollSettingsRequest request) {
        return ResponseEntity.ok(payrollUseCase.configureSettings(companyId, PayrollRestMapper.toCommand(request)));
    }

    @GetMapping("/workers")
    public ResponseEntity<List<Worker>> workers(@RequestHeader(COMPANY_HEADER) UUID companyId) {
        return ResponseEntity.ok(payrollUseCase.workers(companyId));
    }

    @PostMapping("/workers")
    public ResponseEntity<Worker> registerWorker(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody WorkerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payrollUseCase.registerWorker(companyId, PayrollRestMapper.toCommand(request)));
    }

    @GetMapping("/daily-payments")
    public ResponseEntity<List<DailyLaborPayment>> dailyPayments(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return ResponseEntity.ok(payrollUseCase.dailyPayments(companyId, from, to));
    }

    @PostMapping("/daily-payments")
    public ResponseEntity<DailyLaborPayment> registerDailyPayment(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody DailyLaborPaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payrollUseCase.registerDailyPayment(companyId, PayrollRestMapper.toCommand(request)));
    }

    @GetMapping("/electronic-documents")
    public ResponseEntity<List<ElectronicPayrollDocument>> electronicDocuments(@RequestHeader(COMPANY_HEADER) UUID companyId) {
        return ResponseEntity.ok(payrollUseCase.electronicDocuments(companyId));
    }

    @PostMapping("/electronic-documents")
    public ResponseEntity<ElectronicPayrollDocument> issueElectronicDocument(@RequestHeader(COMPANY_HEADER) UUID companyId,
            @Valid @RequestBody ElectronicPayrollDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payrollUseCase.issueElectronicDocument(companyId, PayrollRestMapper.toCommand(request)));
    }
}

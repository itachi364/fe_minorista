package com.msvanegasg.facturaelectronica.accounting.exception;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class AccountingExceptionHandler {

    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final String ACCOUNTING_RULE_NOT_FOUND = "accounting rule was not found";
    private static final String ACCOUNTING_RULE_REQUIRED_MESSAGE =
            "Debes inicializar la configuracion contable basica antes de cerrar ventas.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<AccountingApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<AccountingApiErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new AccountingApiErrorDetail(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(AccountingApiErrorDetail::field))
                .toList();
        return build(HttpStatus.BAD_REQUEST, AccountingApiErrorCode.VALIDATION_ERROR,
                "La solicitud no cumple las reglas de validacion.", details, request);
    }

    @ExceptionHandler({ IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class, HttpMessageNotReadableException.class })
    ResponseEntity<AccountingApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, AccountingApiErrorCode.VALIDATION_ERROR,
                "La solicitud no cumple las reglas de validacion.", List.of(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<AccountingApiErrorResponse> handleBusiness(IllegalStateException exception,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, AccountingApiErrorCode.BUSINESS_RULE_VIOLATION,
                toBusinessMessage(exception), List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<AccountingApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, AccountingApiErrorCode.INTERNAL_ERROR,
                "Ha ocurrido un error inesperado.", List.of(), request);
    }

    private String toBusinessMessage(IllegalStateException exception) {
        if (ACCOUNTING_RULE_NOT_FOUND.equals(exception.getMessage())) {
            return ACCOUNTING_RULE_REQUIRED_MESSAGE;
        }
        return exception.getMessage();
    }

    private ResponseEntity<AccountingApiErrorResponse> build(HttpStatus status, AccountingApiErrorCode code,
            String message, List<AccountingApiErrorDetail> details, HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        return ResponseEntity.status(status).body(new AccountingApiErrorResponse(Instant.now(), status.value(), code,
                message, correlationId, details));
    }
}

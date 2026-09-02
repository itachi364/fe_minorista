package com.msvanegasg.facturaelectronica.tenant.exception;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyAlreadyExistsException;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyBrandingAssetNotFoundException;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyFileAssetNotFoundException;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyLicenseNotFoundException;
import com.msvanegasg.facturaelectronica.tenant.application.usecase.CompanyNotFoundException;
import com.msvanegasg.facturaelectronica.tenant.observability.CorrelationId;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class TenantExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiErrorDetail(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(ApiErrorDetail::field))
                .toList();
        return build(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR,
                "La solicitud no cumple las reglas de validacion.", details, request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR,
                "La solicitud no cumple las reglas de validacion.", List.of(), request);
    }

    @ExceptionHandler({
            CompanyNotFoundException.class,
            CompanyLicenseNotFoundException.class,
            CompanyFileAssetNotFoundException.class,
            CompanyBrandingAssetNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage(), List.of(),
                request);
    }

    @ExceptionHandler(CompanyAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(CompanyAlreadyExistsException exception,
            HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ApiErrorCode.DUPLICATE_RESOURCE, exception.getMessage(), List.of(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR,
                "Ha ocurrido un error inesperado.", List.of(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, ApiErrorCode code, String message,
            List<ApiErrorDetail> details, HttpServletRequest request) {
        String correlationId = correlationId(request);
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                correlationId,
                details));
    }

    private String correlationId(HttpServletRequest request) {
        Object requestCorrelationId = request.getAttribute(CorrelationId.REQUEST_ATTRIBUTE);
        if (requestCorrelationId instanceof String value && !value.isBlank()) {
            return value;
        }
        return CorrelationId.resolve(request.getHeader(CorrelationId.HEADER_NAME));
    }
}

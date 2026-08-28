package com.msvanegasg.facturaelectronica.reporting.interfaces.rest;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.msvanegasg.facturaelectronica.reporting.application.usecase.ReportNotFoundException;
import com.msvanegasg.facturaelectronica.reporting.application.usecase.ReportExportJobNotFoundException;

@RestControllerAdvice
public class ReportingExceptionHandler {

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiError> badRequest(Exception exception) {
        return ResponseEntity.badRequest().body(new ApiError(Instant.now(), 400, "VALIDATION_ERROR",
                exception.getMessage() == null ? "La solicitud no cumple las reglas de validacion."
                        : exception.getMessage()));
    }

    @ExceptionHandler({ ReportNotFoundException.class, ReportExportJobNotFoundException.class })
    public ResponseEntity<ApiError> notFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(Instant.now(), 404,
                "RESOURCE_NOT_FOUND", exception.getMessage()));
    }

    public record ApiError(Instant timestamp, int status, String code, String message) {
    }
}

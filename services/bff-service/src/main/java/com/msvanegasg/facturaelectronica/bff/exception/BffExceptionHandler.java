package com.msvanegasg.facturaelectronica.bff.exception;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.msvanegasg.facturaelectronica.bff.domain.model.UnsupportedBffRouteException;
import com.msvanegasg.facturaelectronica.bff.infrastructure.client.BffAccessDeniedException;
import com.msvanegasg.facturaelectronica.bff.infrastructure.client.DownstreamServiceException;
import com.msvanegasg.facturaelectronica.bff.observability.CorrelationId;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class BffExceptionHandler {

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

    @ExceptionHandler(UnsupportedBffRouteException.class)
    public ResponseEntity<ApiErrorResponse> handleUnsupportedRoute(UnsupportedBffRouteException exception,
            HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ApiErrorCode.RESOURCE_NOT_FOUND, exception.getMessage(), List.of(), request);
    }

    @ExceptionHandler(BffAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(BffAccessDeniedException exception,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ApiErrorCode.ACCESS_DENIED,
                "No tienes permisos suficientes para realizar esta accion.", List.of(), request);
    }

    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<ApiErrorResponse> handleDownstream(DownstreamServiceException exception,
            HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, ApiErrorCode.EXTERNAL_PROVIDER_ERROR,
                "Un servicio interno requerido no esta disponible o no respondio correctamente.", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR,
                "Ocurrio un error interno procesando la solicitud.", List.of(), request);
    }

    private static ResponseEntity<ApiErrorResponse> build(HttpStatus status, ApiErrorCode code, String message,
            List<ApiErrorDetail> details, HttpServletRequest request) {
        String correlationId = String.valueOf(request.getAttribute(CorrelationId.REQUEST_ATTRIBUTE));
        return ResponseEntity.status(status).body(new ApiErrorResponse(Instant.now(), status.value(), code, message,
                correlationId, details));
    }
}

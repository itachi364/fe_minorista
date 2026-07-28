package com.msvanegasg.facturaelectronica.exception;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.msvanegasg.facturaelectronica.exception.compra.CompraNotFoundException;
import com.msvanegasg.facturaelectronica.exception.gasto.GastoInactivoException;
import com.msvanegasg.facturaelectronica.exception.gasto.GastoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoCodigoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoIdNotFoundException;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNotFoundException;
import com.msvanegasg.facturaelectronica.observability.CorrelationId;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String SAFE_INTERNAL_ERROR_MESSAGE = "Ha ocurrido un error inesperado.";
    private static final String SAFE_EXTERNAL_PROVIDER_ERROR_MESSAGE =
            "El proveedor tecnologico no pudo procesar la solicitud.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        List<ApiErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiErrorDetail(error.getField(), error.getDefaultMessage()))
                .sorted(Comparator.comparing(ApiErrorDetail::field))
                .toList();

        return buildResponseEntity(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                "La solicitud no cumple las reglas de validacion.",
                details,
                request);
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleValidationException(Exception exception, HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                safeMessage(exception.getMessage(), "La solicitud no cumple las reglas de validacion."),
                List.of(),
                request);
    }

    @ExceptionHandler({
            CompraNotFoundException.class,
            CategoriaNotFoundException.class,
            GastoNotFoundException.class,
            ImpuestoNotFoundException.class,
            PaisNotFoundException.class,
            MetodoPagoNotFoundException.class,
            ParametroNotFoundException.class,
            TipoDocumentoNotFoundException.class,
            TipoGastoNotFoundException.class,
            ProductoCodigoNotFoundException.class,
            ProductoIdNotFoundException.class
    })
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(RuntimeException exception,
            HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.NOT_FOUND,
                ApiErrorCode.RESOURCE_NOT_FOUND,
                safeMessage(exception.getMessage(), "El recurso solicitado no fue encontrado."),
                List.of(),
                request);
    }

    @ExceptionHandler({
            IllegalStateException.class,
            GastoInactivoException.class,
            TipoDocumentoNoModificableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleException(RuntimeException exception,
            HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.BUSINESS_RULE_VIOLATION,
                safeMessage(exception.getMessage(), "La operacion no cumple una regla de negocio."),
                List.of(),
                request);
    }

    @ExceptionHandler(ExternalProviderException.class)
    public ResponseEntity<ApiErrorResponse> handleExternalProviderException(ExternalProviderException exception,
            HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.BAD_GATEWAY,
                ApiErrorCode.EXTERNAL_PROVIDER_ERROR,
                SAFE_EXTERNAL_PROVIDER_ERROR_MESSAGE,
                List.of(),
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception exception, HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_ERROR,
                SAFE_INTERNAL_ERROR_MESSAGE,
                List.of(),
                request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponseEntity(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            List<ApiErrorDetail> details,
            HttpServletRequest request) {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                code,
                message,
                correlationId(request),
                details);
        return ResponseEntity.status(status).body(response);
    }

    private String correlationId(HttpServletRequest request) {
        Object generatedCorrelationId = request.getAttribute(CorrelationId.REQUEST_ATTRIBUTE);
        if (generatedCorrelationId instanceof String correlationId && !correlationId.isBlank()) {
            return correlationId;
        }
        return CorrelationId.resolve(request.getHeader(CorrelationId.HEADER_NAME));
    }

    private String safeMessage(String message, String defaultMessage) {
        if (message == null || message.isBlank()) {
            return defaultMessage;
        }
        return message;
    }
}

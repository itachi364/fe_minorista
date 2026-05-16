package com.msvanegasg.facturaelectronica.exception;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.msvanegasg.facturaelectronica.exception.cliente.ClienteAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteInactivoException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteNotFoundException;
import com.msvanegasg.facturaelectronica.exception.compra.CompraNotFoundException;
import com.msvanegasg.facturaelectronica.exception.gasto.GastoInactivoException;
import com.msvanegasg.facturaelectronica.exception.gasto.GastoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoCodigoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoIdNotFoundException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorDocumentoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorNotFoundException;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.util.DigitoVerificacionNoModificableException;
import com.msvanegasg.facturaelectronica.exception.util.NitInvalidoException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
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
            ProveedorDocumentoNotFoundException.class,
            ProveedorNotFoundException.class,
            TipoDocumentoNotFoundException.class,
            TipoGastoNotFoundException.class,
            ClienteNotFoundException.class,
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
            ClienteAlreadyExistsException.class,
            ProveedorAlreadyExistsException.class
    })
    public ResponseEntity<ApiErrorResponse> handleDuplicateException(RuntimeException exception,
            HttpServletRequest request) {
        return buildResponseEntity(
                HttpStatus.CONFLICT,
                ApiErrorCode.DUPLICATE_RESOURCE,
                safeMessage(exception.getMessage(), "El recurso ya existe."),
                List.of(),
                request);
    }

    @ExceptionHandler({
            IllegalStateException.class,
            TipoClienteInvalidoException.class,
            GastoInactivoException.class,
            ClienteInactivoException.class,
            ClienteDocumentoNoModificableException.class,
            ProveedorDocumentoNoModificableException.class,
            DigitoVerificacionNoModificableException.class,
            TipoDocumentoNoModificableException.class,
            NitInvalidoException.class
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
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private String safeMessage(String message, String defaultMessage) {
        if (message == null || message.isBlank()) {
            return defaultMessage;
        }
        return message;
    }
}

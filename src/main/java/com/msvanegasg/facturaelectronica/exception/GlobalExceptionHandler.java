package com.msvanegasg.facturaelectronica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ImpuestoNotFoundException.class)
    public ResponseEntity<Object> handleImpuestoNotFoundException(ImpuestoNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(PaisNotFoundException.class)
    public ResponseEntity<Object> handlePaisNotFoundException(PaisNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(MetodoPagoNotFoundException.class)
    public ResponseEntity<Object> handleMetodoPagoNotFountException(MetodoPagoNotFoundException ex){
    	return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(ParametroNotFoundException.class)
    public ResponseEntity<Object> handleParametroNotFountException(ParametroNotFoundException ex){
    	return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(ProveedorNotFoundException.class)
    public ResponseEntity<Object> handleProveedorNotFountException(ProveedorNotFoundException ex){
    	return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(TipoDocumentoNotFoundException.class)
    public ResponseEntity<Object> handleTipoDocumentoNotFountException(TipoDocumentoNotFoundException ex){
    	return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(TipoGastoNotFoundException.class)
    public ResponseEntity<Object> handleTipoGastoNotFountException(TipoGastoNotFoundException ex){
    	return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
    
    @ExceptionHandler(ClienteNotFoundException.class)
    public ResponseEntity<Object> handleClienteNotFountException(ClienteNotFoundException ex){
    	return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado. "+ex);
    }
    
    @ExceptionHandler(ClienteAlreadyExistsException.class)
    public ResponseEntity<String> handleClienteAlreadyExists(ClienteAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    @ExceptionHandler(ProveedorAlreadyExistsException.class)
    public ResponseEntity<String> handleProveedorAlreadyExists(ProveedorAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    
    


    private ResponseEntity<Object> buildResponseEntity(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}
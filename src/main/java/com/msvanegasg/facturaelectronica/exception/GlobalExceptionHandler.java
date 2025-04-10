package com.msvanegasg.facturaelectronica.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.msvanegasg.facturaelectronica.exception.cliente.*;
import com.msvanegasg.facturaelectronica.exception.gasto.*;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.*;
import com.msvanegasg.facturaelectronica.exception.proveedor.*;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.*;
import com.msvanegasg.facturaelectronica.exception.util.*;
import com.msvanegasg.facturaelectronica.exception.compra.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return buildResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado. "+ex);
    }
	
	@ExceptionHandler(CompraNotFoundException.class)
    public ResponseEntity<Object> handleCompraNotFoundException(CompraNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
	
	@ExceptionHandler(CategoriaNotFoundException.class)
    public ResponseEntity<Object> handleCategoriaNotFoundException(CategoriaNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }
	
	@ExceptionHandler(GastoNotFoundException.class)
    public ResponseEntity<Object> handleGastoNotFoundException(GastoNotFoundException ex) {
        return buildResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

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
    
    @ExceptionHandler(ProveedorDocumentoNotFoundException.class)
    public ResponseEntity<Object> handleProveedorDocumentoNotFountException(ProveedorDocumentoNotFoundException ex){
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
    
    @ExceptionHandler(ProductoCodigoNotFoundException.class)
    public ResponseEntity<String> handleProductoNotFoundException(ProductoCodigoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    
    @ExceptionHandler(ClienteAlreadyExistsException.class)
    public ResponseEntity<String> handleClienteAlreadyExists(ClienteAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    @ExceptionHandler(ProveedorAlreadyExistsException.class)
    public ResponseEntity<String> handleProveedorAlreadyExists(ProveedorAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }
    
    @ExceptionHandler(TipoClienteInvalidoException.class)
    public ResponseEntity<String> handleTipoClienteInvalidoException(TipoClienteInvalidoException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(GastoInactivoException.class)
    public ResponseEntity<String> handleGastoInactivoException(GastoInactivoException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ClienteInactivoException.class)
    public ResponseEntity<String> handleClienteInactivoException(ClienteInactivoException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ClienteDocumentoNoModificableException.class)
    public ResponseEntity<String> handleClienteDocumentoNoModificableException(ClienteDocumentoNoModificableException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(ProveedorDocumentoNoModificableException.class)
    public ResponseEntity<String> handleProveedorDocumentoNoModificableException(ProveedorDocumentoNoModificableException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(DigitoVerificacionNoModificableException.class)
    public ResponseEntity<String> handleDigitoVerificacionNoModificableException(DigitoVerificacionNoModificableException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TipoDocumentoNoModificableException.class)
    public ResponseEntity<String> handleTipoDocumentoNoModificableException(TipoDocumentoNoModificableException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(NitInvalidoException.class)
    public ResponseEntity<String> handleNitInvalidoException(NitInvalidoException ex) {
        return ResponseEntity.internalServerError().body(ex.getMessage());
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
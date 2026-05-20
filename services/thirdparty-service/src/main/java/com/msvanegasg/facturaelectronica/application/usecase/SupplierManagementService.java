package com.msvanegasg.facturaelectronica.thirdparty.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorDocumentoNotFoundException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorNotFoundException;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.util.DigitoVerificacionNoModificableException;
import com.msvanegasg.facturaelectronica.exception.util.NitInvalidoException;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.SupplierCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageSupplierUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.DocumentTypeLookupPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.SupplierRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;
import com.msvanegasg.facturaelectronica.util.NitValidatorUtil;

public class SupplierManagementService implements ManageSupplierUseCase {

    private final SupplierRepositoryPort supplierRepository;
    private final DocumentTypeLookupPort documentTypeLookup;

    public SupplierManagementService(SupplierRepositoryPort supplierRepository, DocumentTypeLookupPort documentTypeLookup) {
        this.supplierRepository = Objects.requireNonNull(supplierRepository);
        this.documentTypeLookup = Objects.requireNonNull(documentTypeLookup);
    }

    @Override
    public List<Supplier> findAll() {
        return supplierRepository.findAll();
    }

    @Override
    public List<Supplier> findActive() {
        return supplierRepository.findActive();
    }

    @Override
    public List<Supplier> findInactive() {
        return supplierRepository.findInactive();
    }

    @Override
    public Supplier findById(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ProveedorNotFoundException(id));
    }

    @Override
    public Supplier findByDocument(Long documentTypeId, Long documentNumber) {
        return findSupplier(documentTypeId, documentNumber);
    }

    @Override
    public Supplier findByName(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name)
                .orElseThrow(() -> new ProveedorNotFoundException(null));
    }

    @Override
    public Supplier create(SupplierCommand command) {
        Objects.requireNonNull(command, "command is required");
        DocumentTypeSummary documentType = documentTypeLookup.findByCode(command.documentTypeId());
        validateNit(command.documentTypeId(), command.documentNumber(), command.verificationDigit());
        if (supplierRepository.existsByDocumentNumber(command.documentNumber())) {
            throw new ProveedorAlreadyExistsException(command.documentNumber(), command.documentTypeId());
        }
        Supplier supplier = Supplier.create(
                command.name(),
                documentType,
                command.documentNumber(),
                command.verificationDigit(),
                command.address(),
                command.phone(),
                command.email());
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier update(Long documentTypeId, Long documentNumber, SupplierCommand command) {
        Objects.requireNonNull(command, "command is required");
        Supplier existing = findSupplier(documentTypeId, documentNumber);
        validateNonModifiableIdentity(existing, command);
        Supplier updated = existing.updateProfile(
                command.name(),
                command.verificationDigit(),
                command.address(),
                command.phone(),
                command.email());
        return supplierRepository.save(updated);
    }

    @Override
    public void disable(Long documentTypeId, Long documentNumber) {
        Supplier existing = findSupplier(documentTypeId, documentNumber);
        supplierRepository.save(existing.disable());
    }

    @Override
    public void enable(Long documentTypeId, Long documentNumber) {
        Supplier existing = findSupplier(documentTypeId, documentNumber);
        if (!existing.active()) {
            supplierRepository.save(existing.enable());
        }
    }

    private Supplier findSupplier(Long documentTypeId, Long documentNumber) {
        return supplierRepository.findByDocumentNumberAndDocumentTypeId(documentNumber, documentTypeId)
                .orElseThrow(() -> new ProveedorDocumentoNotFoundException(documentNumber, documentTypeId));
    }

    private static void validateNit(Long documentTypeId, Long documentNumber, Integer verificationDigit) {
        if ((documentTypeId == 31 || documentTypeId == 50)
                && !NitValidatorUtil.esNitValido(documentTypeId, documentNumber, Optional.ofNullable(verificationDigit))) {
            throw new NitInvalidoException(documentNumber);
        }
    }

    private static void validateNonModifiableIdentity(Supplier existing, SupplierCommand command) {
        if (!Objects.equals(existing.documentNumber(), command.documentNumber())) {
            throw new ProveedorDocumentoNoModificableException(command.documentNumber());
        }
        if (!Objects.equals(existing.documentType().id(), command.documentTypeId())) {
            throw new TipoDocumentoNoModificableException(command.documentTypeId());
        }
        if (!Objects.equals(existing.verificationDigit(), command.verificationDigit())) {
            throw new DigitoVerificacionNoModificableException("proveedor");
        }
    }
}

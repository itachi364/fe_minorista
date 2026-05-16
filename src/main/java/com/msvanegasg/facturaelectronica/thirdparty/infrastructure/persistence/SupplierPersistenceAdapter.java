package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.infrastructure.persistence.entity.DocumentTypeJpaEntity;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.SupplierRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity.SupplierJpaEntity;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository.SupplierJpaRepository;

@Component
public class SupplierPersistenceAdapter implements SupplierRepositoryPort {

    private final SupplierJpaRepository supplierRepository;

    public SupplierPersistenceAdapter(SupplierJpaRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public List<Supplier> findAll() {
        return supplierRepository.findAll().stream()
                .map(SupplierPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Supplier> findActive() {
        return supplierRepository.findByActivoTrue().stream()
                .map(SupplierPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Supplier> findInactive() {
        return supplierRepository.findByActivoFalse().stream()
                .map(SupplierPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id)
                .map(SupplierPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Supplier> findByDocumentNumberAndDocumentTypeId(Long documentNumber, Long documentTypeId) {
        return supplierRepository.findByNumeroDocumentoAndTipoDocumento(documentNumber, documentType(documentTypeId, null))
                .map(SupplierPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<Supplier> findByNameContainingIgnoreCase(String name) {
        return Optional.ofNullable(supplierRepository.findByNombreContainingIgnoreCase(name))
                .map(SupplierPersistenceAdapter::toDomain);
    }

    @Override
    public boolean existsByDocumentNumber(Long documentNumber) {
        return supplierRepository.existsByNumeroDocumento(documentNumber);
    }

    @Override
    public Supplier save(Supplier supplier) {
        SupplierJpaEntity saved = supplierRepository.save(toEntity(supplier));
        return toDomain(saved);
    }

    private static Supplier toDomain(SupplierJpaEntity entity) {
        return Supplier.restore(
                entity.getIdProveedor(),
                entity.getNombre(),
                toDocumentTypeSummary(entity.getTipoDocumento()),
                entity.getNumeroDocumento(),
                entity.getDigitoVerificacion(),
                entity.getDireccion(),
                entity.getTelefono(),
                entity.getCorreoElectronico(),
                Boolean.TRUE.equals(entity.getActivo()));
    }

    private static SupplierJpaEntity toEntity(Supplier supplier) {
        return SupplierJpaEntity.builder()
                .idProveedor(supplier.id())
                .nombre(supplier.name())
                .tipoDocumento(documentType(supplier.documentType().id(), supplier.documentType().name()))
                .numeroDocumento(supplier.documentNumber())
                .digitoVerificacion(supplier.verificationDigit())
                .direccion(supplier.address())
                .telefono(supplier.phone())
                .correoElectronico(supplier.email())
                .activo(supplier.active())
                .build();
    }

    private static DocumentTypeSummary toDocumentTypeSummary(DocumentTypeJpaEntity entity) {
        return new DocumentTypeSummary(entity.getCodigo(), entity.getNombre());
    }

    private static DocumentTypeJpaEntity documentType(Long code, String name) {
        return DocumentTypeJpaEntity.builder()
                .codigo(code)
                .nombre(name)
                .activo(true)
                .build();
    }
}

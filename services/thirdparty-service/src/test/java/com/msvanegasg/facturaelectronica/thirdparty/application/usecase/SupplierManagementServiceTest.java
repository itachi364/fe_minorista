package com.msvanegasg.facturaelectronica.thirdparty.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.SupplierCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.DocumentTypeLookupPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.SupplierRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;

class SupplierManagementServiceTest {

    @Test
    void createSupplierStartsActiveAndEnrichesDocumentType() {
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        SupplierManagementService service = new SupplierManagementService(repository, new FakeDocumentTypeLookup());

        Supplier result = service.create(command("Proveedor Prueba"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Proveedor Prueba");
        assertThat(result.documentType().id()).isEqualTo(13L);
        assertThat(result.documentType().name()).isEqualTo("Cedula");
        assertThat(result.active()).isTrue();
    }

    @Test
    void createSupplierRejectsExistingDocument() {
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        repository.save(supplier(7L, true));
        SupplierManagementService service = new SupplierManagementService(repository, new FakeDocumentTypeLookup());

        assertThatThrownBy(() -> service.create(command("Proveedor Duplicado")))
                .isInstanceOf(ProveedorAlreadyExistsException.class);
    }

    @Test
    void updateSupplierRejectsDocumentNumberChanges() {
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        repository.save(supplier(7L, true));
        SupplierManagementService service = new SupplierManagementService(repository, new FakeDocumentTypeLookup());

        SupplierCommand command = new SupplierCommand(13L, 987654321L, null, "Proveedor Actualizado", "3009998888",
                "Calle 2", "proveedor2@example.com");

        assertThatThrownBy(() -> service.update(13L, 123456789L, command))
                .isInstanceOf(ProveedorDocumentoNoModificableException.class);
    }

    @Test
    void disableAndEnableSupplier() {
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        repository.save(supplier(7L, true));
        SupplierManagementService service = new SupplierManagementService(repository, new FakeDocumentTypeLookup());

        service.disable(13L, 123456789L);
        assertThat(repository.findByDocumentNumberAndDocumentTypeId(123456789L, 13L).orElseThrow().active()).isFalse();

        service.enable(13L, 123456789L);
        assertThat(repository.findByDocumentNumberAndDocumentTypeId(123456789L, 13L).orElseThrow().active()).isTrue();
    }

    @Test
    void findByNameReturnsSupplier() {
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        repository.save(supplier(7L, true));
        SupplierManagementService service = new SupplierManagementService(repository, new FakeDocumentTypeLookup());

        Supplier result = service.findByName("Proveedor");

        assertThat(result.documentType().name()).isEqualTo("Cedula");
    }

    private static SupplierCommand command(String name) {
        return new SupplierCommand(
                13L,
                123456789L,
                null,
                name,
                "3001234567",
                "Calle 1",
                "proveedor@example.com");
    }

    private static Supplier supplier(Long id, boolean active) {
        return Supplier.restore(
                id,
                "Proveedor Prueba",
                new DocumentTypeSummary(13L, "Cedula"),
                123456789L,
                null,
                "Calle 1",
                "3001234567",
                "proveedor@example.com",
                active);
    }

    private static final class InMemorySupplierRepository implements SupplierRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, Supplier> suppliers = new LinkedHashMap<>();

        @Override
        public List<Supplier> findAll() {
            return List.copyOf(suppliers.values());
        }

        @Override
        public List<Supplier> findActive() {
            return suppliers.values().stream().filter(Supplier::active).toList();
        }

        @Override
        public List<Supplier> findInactive() {
            return suppliers.values().stream().filter(supplier -> !supplier.active()).toList();
        }

        @Override
        public Optional<Supplier> findById(Long id) {
            return Optional.ofNullable(suppliers.get(id));
        }

        @Override
        public Optional<Supplier> findByDocumentNumberAndDocumentTypeId(Long documentNumber, Long documentTypeId) {
            return suppliers.values().stream()
                    .filter(supplier -> supplier.documentNumber().equals(documentNumber)
                            && supplier.documentType().id().equals(documentTypeId))
                    .findFirst();
        }

        @Override
        public Optional<Supplier> findByNameContainingIgnoreCase(String name) {
            return suppliers.values().stream()
                    .filter(supplier -> supplier.name().toLowerCase().contains(name.toLowerCase()))
                    .findFirst();
        }

        @Override
        public boolean existsByDocumentNumber(Long documentNumber) {
            return suppliers.values().stream()
                    .anyMatch(supplier -> supplier.documentNumber().equals(documentNumber));
        }

        @Override
        public Supplier save(Supplier supplier) {
            Supplier toSave = supplier.id() == null
                    ? Supplier.restore(nextId++, supplier.name(), supplier.documentType(), supplier.documentNumber(),
                            supplier.verificationDigit(), supplier.address(), supplier.phone(), supplier.email(),
                            supplier.active())
                    : supplier;
            suppliers.put(toSave.id(), toSave);
            return toSave;
        }
    }

    private static final class FakeDocumentTypeLookup implements DocumentTypeLookupPort {

        @Override
        public DocumentTypeSummary findByCode(Long code) {
            return new DocumentTypeSummary(code, "Cedula");
        }
    }
}

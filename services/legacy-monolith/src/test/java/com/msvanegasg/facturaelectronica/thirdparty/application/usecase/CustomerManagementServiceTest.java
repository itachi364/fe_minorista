package com.msvanegasg.facturaelectronica.thirdparty.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.enums.TipoClienteEnum;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteInactivoException;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerResult;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.CustomerRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.DocumentTypeLookupPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Customer;

class CustomerManagementServiceTest {

    @Test
    void createCustomerStartsActiveAndEnrichesDocumentType() {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        CustomerManagementService service = new CustomerManagementService(repository, new FakeDocumentTypeLookup());

        CustomerResult result = service.create(command("Cliente Prueba"));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Cliente Prueba");
        assertThat(result.documentTypeId()).isEqualTo(13L);
        assertThat(result.documentTypeCode()).isEqualTo("13");
        assertThat(result.documentTypeDescription()).isEqualTo("Cedula");
        assertThat(result.customerType()).isEqualTo("NATURAL");
        assertThat(result.active()).isTrue();
    }

    @Test
    void createCustomerRejectsExistingDocument() {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        repository.save(customer(7L, true));
        CustomerManagementService service = new CustomerManagementService(repository, new FakeDocumentTypeLookup());

        assertThatThrownBy(() -> service.create(command("Cliente Duplicado")))
                .isInstanceOf(ClienteAlreadyExistsException.class);
    }

    @Test
    void updateCustomerRejectsInactiveCustomer() {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        repository.save(customer(7L, false));
        CustomerManagementService service = new CustomerManagementService(repository, new FakeDocumentTypeLookup());

        assertThatThrownBy(() -> service.update(13L, 123456789L, command("Cliente Actualizado")))
                .isInstanceOf(ClienteInactivoException.class);
    }

    @Test
    void disableAndEnableCustomer() {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        repository.save(customer(7L, true));
        CustomerManagementService service = new CustomerManagementService(repository, new FakeDocumentTypeLookup());

        service.disable(13L, 123456789L);
        assertThat(repository.findByDocumentTypeIdAndDocumentNumber(13L, 123456789L).orElseThrow().active()).isFalse();

        service.enable(13L, 123456789L);
        assertThat(repository.findByDocumentTypeIdAndDocumentNumber(13L, 123456789L).orElseThrow().active()).isTrue();
    }

    @Test
    void findByNameReturnsEnrichedResults() {
        InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
        repository.save(customer(7L, true));
        CustomerManagementService service = new CustomerManagementService(repository, new FakeDocumentTypeLookup());

        List<CustomerResult> results = service.findByName("Cliente");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).documentTypeDescription()).isEqualTo("Cedula");
    }

    private static CustomerCommand command(String name) {
        return new CustomerCommand(
                name,
                13L,
                123456789L,
                null,
                "Calle 1",
                "3001234567",
                "cliente@example.com");
    }

    private static Customer customer(Long id, boolean active) {
        return Customer.restore(
                id,
                "Cliente Prueba",
                13L,
                123456789L,
                null,
                "Calle 1",
                "3001234567",
                "cliente@example.com",
                TipoClienteEnum.NATURAL,
                active);
    }

    private static final class InMemoryCustomerRepository implements CustomerRepositoryPort {

        private long nextId = 1L;
        private final Map<Long, Customer> customers = new LinkedHashMap<>();

        @Override
        public List<Customer> findAllByActive(boolean active) {
            return customers.values().stream().filter(customer -> customer.active() == active).toList();
        }

        @Override
        public List<Customer> findByNameContainingIgnoreCase(String name) {
            return customers.values().stream()
                    .filter(customer -> customer.name().toLowerCase().contains(name.toLowerCase()))
                    .toList();
        }

        @Override
        public Optional<Customer> findByDocumentTypeIdAndDocumentNumber(Long documentTypeId, Long documentNumber) {
            return customers.values().stream()
                    .filter(customer -> customer.documentTypeId().equals(documentTypeId)
                            && customer.documentNumber().equals(documentNumber))
                    .findFirst();
        }

        @Override
        public boolean existsByDocumentNumberAndDocumentTypeId(Long documentNumber, Long documentTypeId) {
            return findByDocumentTypeIdAndDocumentNumber(documentTypeId, documentNumber).isPresent();
        }

        @Override
        public Customer save(Customer customer) {
            Customer toSave = customer.id() == null
                    ? Customer.restore(nextId++, customer.name(), customer.documentTypeId(), customer.documentNumber(),
                            customer.verificationDigit(), customer.address(), customer.phone(), customer.email(),
                            customer.customerType(), customer.active())
                    : customer;
            customers.put(toSave.id(), toSave);
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

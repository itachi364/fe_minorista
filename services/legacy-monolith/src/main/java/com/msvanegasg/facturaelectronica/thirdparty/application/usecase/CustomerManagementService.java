package com.msvanegasg.facturaelectronica.thirdparty.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.msvanegasg.facturaelectronica.enums.TipoClienteEnum;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteAlreadyExistsException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteInactivoException;
import com.msvanegasg.facturaelectronica.exception.cliente.ClienteNotFoundException;
import com.msvanegasg.facturaelectronica.exception.cliente.TipoClienteNoReconocidoException;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNoModificableException;
import com.msvanegasg.facturaelectronica.exception.util.DigitoVerificacionNoModificableException;
import com.msvanegasg.facturaelectronica.exception.util.NitInvalidoException;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerResult;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageCustomerUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.CustomerRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.DocumentTypeLookupPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Customer;
import com.msvanegasg.facturaelectronica.util.NitValidatorUtil;

public class CustomerManagementService implements ManageCustomerUseCase {

    private final CustomerRepositoryPort customerRepository;
    private final DocumentTypeLookupPort documentTypeLookup;

    public CustomerManagementService(CustomerRepositoryPort customerRepository, DocumentTypeLookupPort documentTypeLookup) {
        this.customerRepository = Objects.requireNonNull(customerRepository);
        this.documentTypeLookup = Objects.requireNonNull(documentTypeLookup);
    }

    @Override
    public List<CustomerResult> findActive() {
        return customerRepository.findAllByActive(true).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public List<CustomerResult> findInactive() {
        return customerRepository.findAllByActive(false).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public List<CustomerResult> findByName(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public CustomerResult findByDocument(Long documentTypeId, Long documentNumber) {
        return toResult(findCustomer(documentTypeId, documentNumber));
    }

    @Override
    public CustomerResult create(CustomerCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (customerRepository.existsByDocumentNumberAndDocumentTypeId(command.documentNumber(), command.documentTypeId())) {
            throw new ClienteAlreadyExistsException(command.documentNumber(), command.documentTypeId());
        }
        TipoClienteEnum customerType = determineCustomerType(command.documentTypeId());
        validateNit(command.documentTypeId(), command.documentNumber(), command.verificationDigit());
        documentTypeLookup.findByCode(command.documentTypeId());

        Customer customer = Customer.create(
                command.name(),
                command.documentTypeId(),
                command.documentNumber(),
                command.verificationDigit(),
                command.address(),
                command.phone(),
                command.email(),
                customerType);
        return toResult(customerRepository.save(customer));
    }

    @Override
    public CustomerResult update(Long documentTypeId, Long documentNumber, CustomerCommand command) {
        Objects.requireNonNull(command, "command is required");
        Customer existing = findCustomer(documentTypeId, documentNumber);
        if (!existing.active()) {
            throw new ClienteInactivoException(documentNumber);
        }
        validateNonModifiableIdentity(existing, command);
        TipoClienteEnum customerType = determineCustomerType(command.documentTypeId());
        validateNit(command.documentTypeId(), command.documentNumber(), command.verificationDigit());
        documentTypeLookup.findByCode(documentTypeId);

        Customer updated = existing.updateProfile(
                command.name(),
                command.verificationDigit(),
                command.address(),
                command.phone(),
                command.email(),
                customerType);
        return toResult(customerRepository.save(updated));
    }

    @Override
    public void disable(Long documentTypeId, Long documentNumber) {
        Customer existing = findCustomer(documentTypeId, documentNumber);
        customerRepository.save(existing.disable());
    }

    @Override
    public void enable(Long documentTypeId, Long documentNumber) {
        Customer existing = findCustomer(documentTypeId, documentNumber);
        customerRepository.save(existing.enable());
    }

    private Customer findCustomer(Long documentTypeId, Long documentNumber) {
        return customerRepository.findByDocumentTypeIdAndDocumentNumber(documentTypeId, documentNumber)
                .orElseThrow(() -> new ClienteNotFoundException(documentNumber, documentTypeId));
    }

    private CustomerResult toResult(Customer customer) {
        DocumentTypeSummary documentType = documentTypeLookup.findByCode(customer.documentTypeId());
        return new CustomerResult(
                customer.id(),
                customer.name(),
                customer.documentTypeId(),
                documentType.id().toString(),
                documentType.name(),
                customer.documentNumber(),
                customer.verificationDigit(),
                customer.address(),
                customer.phone(),
                customer.email(),
                customer.customerType().toString(),
                customer.active());
    }

    private static TipoClienteEnum determineCustomerType(Long documentTypeId) {
        TipoClienteEnum customerType = (documentTypeId == 31 || documentTypeId == 50)
                ? TipoClienteEnum.JURIDICO
                : TipoClienteEnum.NATURAL;
        if (customerType == null) {
            throw new TipoClienteNoReconocidoException(documentTypeId);
        }
        return customerType;
    }

    private static void validateNit(Long documentTypeId, Long documentNumber, Integer verificationDigit) {
        if (!NitValidatorUtil.esNitValido(documentTypeId, documentNumber, Optional.ofNullable(verificationDigit))) {
            throw new NitInvalidoException(documentNumber);
        }
    }

    private static void validateNonModifiableIdentity(Customer existing, CustomerCommand command) {
        if (!Objects.equals(existing.documentNumber(), command.documentNumber())) {
            throw new ClienteDocumentoNoModificableException(command.documentNumber());
        }
        if (!Objects.equals(existing.documentTypeId(), command.documentTypeId())) {
            throw new TipoDocumentoNoModificableException(command.documentTypeId());
        }
        if (!Objects.equals(existing.verificationDigit(), command.verificationDigit())) {
            throw new DigitoVerificacionNoModificableException("cliente");
        }
    }
}

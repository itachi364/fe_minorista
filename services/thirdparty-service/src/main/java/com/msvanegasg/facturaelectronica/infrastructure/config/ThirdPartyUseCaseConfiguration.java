package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageCustomerUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageSupplierUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.CustomerRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.DocumentTypeLookupPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.SupplierRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.application.usecase.CustomerManagementService;
import com.msvanegasg.facturaelectronica.thirdparty.application.usecase.SupplierManagementService;

@Configuration
public class ThirdPartyUseCaseConfiguration {

    @Bean
    ManageCustomerUseCase manageCustomerUseCase(CustomerRepositoryPort customerRepositoryPort,
            DocumentTypeLookupPort documentTypeLookupPort) {
        return new CustomerManagementService(customerRepositoryPort, documentTypeLookupPort);
    }

    @Bean
    ManageSupplierUseCase manageSupplierUseCase(SupplierRepositoryPort supplierRepositoryPort,
            DocumentTypeLookupPort documentTypeLookupPort) {
        return new SupplierManagementService(supplierRepositoryPort, documentTypeLookupPort);
    }
}

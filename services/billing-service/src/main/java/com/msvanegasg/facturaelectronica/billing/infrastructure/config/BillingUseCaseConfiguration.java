package com.msvanegasg.facturaelectronica.billing.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryAvailabilityPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.InventoryMovementPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.SaleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.usecase.SaleManagementService;

@Configuration
public class BillingUseCaseConfiguration {

    @Bean
    ManageSaleUseCase manageSaleUseCase(SaleRepositoryPort saleRepository,
            InventoryAvailabilityPort inventoryAvailability, ElectronicDocumentProviderPort providerPort,
            InventoryMovementPort inventoryMovementPort, AccountingEntryPort accountingEntryPort,
            IdGeneratorPort idGenerator, ClockPort clock) {
        return new SaleManagementService(saleRepository, inventoryAvailability, providerPort, inventoryMovementPort,
                accountingEntryPort, idGenerator, clock);
    }
}

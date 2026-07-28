package com.msvanegasg.facturaelectronica.inventory.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageServiceSupplyReferenceUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.InventoryMovementRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseAccountingPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ServiceSupplyReferenceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.usecase.ProductManagementService;
import com.msvanegasg.facturaelectronica.inventory.application.usecase.PurchaseManagementService;
import com.msvanegasg.facturaelectronica.inventory.application.usecase.RegisterInventoryMovementService;
import com.msvanegasg.facturaelectronica.inventory.application.usecase.ServiceSupplyReferenceManagementService;

import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;

@Configuration
public class InventoryUseCaseConfiguration {

    @Bean
    RegisterInventoryMovementUseCase registerInventoryMovementUseCase(ProductRepositoryPort productRepository,
            StockBalanceRepositoryPort stockBalanceRepository, InventoryMovementRepositoryPort movementRepository,
            DomainEventPublisherPort eventPublisher, IdGeneratorPort idGenerator, ClockPort clock) {
        return new RegisterInventoryMovementService(productRepository, stockBalanceRepository, movementRepository,
                eventPublisher, idGenerator, clock);
    }

    @Bean
    ManageProductUseCase manageProductUseCase(ProductRepositoryPort productRepository,
            StockBalanceRepositoryPort stockBalanceRepository, RegisterInventoryMovementUseCase movementUseCase,
            IdGeneratorPort idGenerator, ClockPort clock) {
        return new ProductManagementService(productRepository, stockBalanceRepository, movementUseCase, idGenerator,
                clock);
    }

    @Bean
    ManagePurchaseUseCase managePurchaseUseCase(PurchaseRepositoryPort purchaseRepository,
            ProductRepositoryPort productRepository, RegisterInventoryMovementUseCase movementUseCase,
            PurchaseAccountingPort purchaseAccountingPort, IdGeneratorPort idGenerator, ClockPort clock) {
        return new PurchaseManagementService(purchaseRepository, productRepository, movementUseCase,
                purchaseAccountingPort, idGenerator, clock);
    }

    @Bean
    ManageServiceSupplyReferenceUseCase manageServiceSupplyReferenceUseCase(
            ServiceSupplyReferenceRepositoryPort referenceRepository, ProductRepositoryPort productRepository,
            IdGeneratorPort idGenerator, ClockPort clock) {
        return new ServiceSupplyReferenceManagementService(referenceRepository, productRepository, idGenerator, clock);
    }
}

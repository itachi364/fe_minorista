package com.msvanegasg.facturaelectronica.inventory.infrastructure.config;

import java.time.Instant;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductLookupPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductStockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.SupplierLookupPort;
import com.msvanegasg.facturaelectronica.inventory.application.usecase.PurchaseManagementService;

@Configuration
public class InventoryUseCaseConfiguration {

    @Bean
    ClockPort inventoryClockPort() {
        return Instant::now;
    }

    @Bean
    ManagePurchaseUseCase managePurchaseUseCase(PurchaseRepositoryPort purchaseRepositoryPort,
            ProductLookupPort productLookupPort, ProductStockPort productStockPort, SupplierLookupPort supplierLookupPort,
            ClockPort inventoryClockPort) {
        return new PurchaseManagementService(purchaseRepositoryPort, productLookupPort, productStockPort,
                supplierLookupPort, inventoryClockPort);
    }
}

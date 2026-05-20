package com.msvanegasg.facturaelectronica.inventory.infrastructure.client;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductStockIncreaseRequest;
import com.msvanegasg.facturaelectronica.client.ProductoClient;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductPurchaseInfo;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductLookupPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductStockPort;

@Component
public class ProductInventoryAdapter implements ProductLookupPort, ProductStockPort {

    private final ProductoClient productoClient;

    public ProductInventoryAdapter(ProductoClient productoClient) {
        this.productoClient = productoClient;
    }

    @Override
    public ProductPurchaseInfo findByBarcode(Long barcode) {
        ProductResponse product = productoClient.obtenerProductoPorCodigoBarrasMigrado(barcode);
        return new ProductPurchaseInfo(product.getId(), product.getCodigoBarras());
    }

    @Override
    public void increaseStock(Long barcode, Integer quantity) {
        productoClient.aumentarStockMigrado(new ProductStockIncreaseRequest(barcode, quantity));
    }
}

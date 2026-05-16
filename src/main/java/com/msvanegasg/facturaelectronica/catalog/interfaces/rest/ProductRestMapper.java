package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.IncreaseProductStockCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.ProductCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Product;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductStockIncreaseRequest;

public final class ProductRestMapper {

    private ProductRestMapper() {
    }

    public static ProductCommand toCommand(ProductRequest dto) {
        return new ProductCommand(
                dto.getNombre(),
                dto.getDescripcion(),
                dto.getPrecioBase(),
                dto.getCantidadStock(),
                dto.getIdCategoria(),
                dto.getCodigoBarras());
    }

    public static IncreaseProductStockCommand toIncreaseStockCommand(ProductStockIncreaseRequest dto) {
        return new IncreaseProductStockCommand(dto.getCodigoBarras(), dto.getCantidadASumar());
    }

    public static ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.id())
                .nombre(product.name())
                .descripcion(product.description())
                .precioBase(product.basePrice())
                .cantidadStock(product.stockQuantity())
                .categoria(toCategoryResponse(product))
                .codigoBarras(product.barcode())
                .build();
    }

    private static ProductResponse.CategorySummary toCategoryResponse(Product product) {
        return ProductResponse.CategorySummary.builder()
                .id(product.category().id())
                .nombre(product.category().name())
                .build();
    }
}

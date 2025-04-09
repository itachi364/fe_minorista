package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.ProductoDTO;
import com.msvanegasg.facturaelectronica.models.Categoria;
import com.msvanegasg.facturaelectronica.models.Producto;
import org.springframework.stereotype.Component;

@Component
public class ProductoMapper {

    public Producto toEntity(ProductoDTO dto, Categoria categoria) {
        return Producto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .precioBase(dto.getPrecioBase())
                .cantidadStock(dto.getCantidadStock())
                .categoria(categoria)
                .activo(true)
                .build();
    }

    public ProductoDTO toDTO(Producto producto) {
        return ProductoDTO.builder()
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precioBase(producto.getPrecioBase())
                .cantidadStock(producto.getCantidadStock())
                .idCategoria(producto.getCategoria().getIdCategoria())
                .build();
    }
}

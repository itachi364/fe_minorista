package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.DTO.CompraDTO;
import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.exception.compra.CompraNotFoundException;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorNotFoundException;
import com.msvanegasg.facturaelectronica.models.Compra;
import com.msvanegasg.facturaelectronica.models.DetalleCompra;
import com.msvanegasg.facturaelectronica.models.Producto;
import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.repository.CompraRepository;
import com.msvanegasg.facturaelectronica.repository.DetalleCompraRepository;
import com.msvanegasg.facturaelectronica.repository.ProductoRepository;
import com.msvanegasg.facturaelectronica.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompraService {

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public Compra crearCompra(CompraDTO compraDTO) {
        Proveedor proveedor = proveedorRepository.findById(compraDTO.getIdProveedor())
                .orElseThrow(() -> new ProveedorNotFoundException(compraDTO.getIdProveedor()));

        Compra compra = Compra.builder()
                .proveedor(proveedor)
                .fecha(LocalDateTime.now())
                .estado(Estado.PROCESADO)
                .activo(true)
                .urlEvidencia(compraDTO.getUrlEvidencia())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalIva = BigDecimal.ZERO;

        compra = compraRepository.save(compra);

        for (var detalleDTO : compraDTO.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getIdProducto()));

            BigDecimal precio = detalleDTO.getPrecioUnitario();
            Integer cantidad = detalleDTO.getCantidad();
            BigDecimal sub = precio.multiply(BigDecimal.valueOf(cantidad));
            BigDecimal iva = sub.multiply(detalleDTO.getIva()).divide(BigDecimal.valueOf(100));
            BigDecimal totalLinea = sub.add(iva);

            subtotal = subtotal.add(sub);
            totalIva = totalIva.add(iva);

            DetalleCompra detalle = DetalleCompra.builder()
                    .compra(compra)
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(precio)
                    .subtotal(sub)
                    .iva(iva)
                    .totalLinea(totalLinea)
                    .build();

            detalleCompraRepository.save(detalle);
        }

        compra.setSubtotal(subtotal);
        compra.setIvaTotal(totalIva);
        compra.setTotal(subtotal.add(totalIva));

        return compraRepository.save(compra);
    }

    @Transactional(readOnly = true)
    public Compra obtenerCompraPorId(Long idCompra) {
        return compraRepository.findById(idCompra)
                .orElseThrow(() -> new CompraNotFoundException(idCompra));
    }

    @Transactional(readOnly = true)
    public List<Compra> listarComprasActivas() {
        return compraRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public List<DetalleCompra> obtenerDetallesPorCompra(Long idCompra) {
        if (!compraRepository.existsById(idCompra)) {
            throw new CompraNotFoundException(idCompra);
        }
        return detalleCompraRepository.findByCompraIdCompra(idCompra);
    }
}

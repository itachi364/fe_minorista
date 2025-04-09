package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.CompraDTO;
import com.msvanegasg.facturaelectronica.DTO.DetalleCompraDTO;
import com.msvanegasg.facturaelectronica.mapper.CompraMapper;
import com.msvanegasg.facturaelectronica.mapper.DetalleCompraMapper;
import com.msvanegasg.facturaelectronica.models.Compra;
import com.msvanegasg.facturaelectronica.models.DetalleCompra;
import com.msvanegasg.facturaelectronica.service.CompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    public ResponseEntity<CompraDTO> registrarCompra(@Valid @RequestBody CompraDTO compraDTO) {
        Compra compraCreada = compraService.crearCompra(compraDTO);
        List<DetalleCompra> detalles = compraService.obtenerDetallesPorCompra(compraCreada.getIdCompra());
        return ResponseEntity.ok(CompraMapper.toDTO(compraCreada, detalles));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraDTO> obtenerCompraPorId(@PathVariable Long id) {
        Compra compra = compraService.obtenerCompraPorId(id);
        List<DetalleCompra> detalles = compraService.obtenerDetallesPorCompra(id);
        return ResponseEntity.ok(CompraMapper.toDTO(compra, detalles));
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<DetalleCompraDTO>> obtenerDetallesDeCompra(@PathVariable Long id) {
        List<DetalleCompra> detalles = compraService.obtenerDetallesPorCompra(id);
        List<DetalleCompraDTO> detallesDTO = detalles.stream()
                .map(DetalleCompraMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(detallesDTO);
    }

    @GetMapping
    public ResponseEntity<List<CompraDTO>> listarComprasActivas() {
        List<Compra> compras = compraService.listarComprasActivas();
        List<CompraDTO> comprasDTO = compras.stream()
                .map(compra -> {
                    List<DetalleCompra> detalles = compraService.obtenerDetallesPorCompra(compra.getIdCompra());
                    return CompraMapper.toDTO(compra, detalles);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(comprasDTO);
    }
}


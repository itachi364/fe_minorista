package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.GastoDTO;
import com.msvanegasg.facturaelectronica.DTO.response.GastoResponseDTO;
import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.mapper.GastoMapper;
import com.msvanegasg.facturaelectronica.models.Gasto;
import com.msvanegasg.facturaelectronica.service.GastoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gastos")
@RequiredArgsConstructor
public class GastoController {

    private final GastoService gastoService;

    @PostMapping
    public ResponseEntity<GastoResponseDTO> crearGasto(@Valid @RequestBody GastoDTO gastoDTO) {
        Gasto nuevoGasto = gastoService.crearGasto(gastoDTO);
        return ResponseEntity.ok(GastoMapper.toResponseDTO(nuevoGasto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> actualizarGasto(@PathVariable("id") Long id,
                                                            @Valid @RequestBody GastoDTO gastoDTO) {
        Gasto actualizado = gastoService.actualizarGasto(id, gastoDTO);
        return ResponseEntity.ok(GastoMapper.toResponseDTO(actualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarGasto(@PathVariable("id") Long id) {
        gastoService.eliminarGasto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GastoResponseDTO> obtenerGasto(@PathVariable("id") Long id) {
        Gasto gasto = gastoService.obtenerGastoPorId(id);
        return ResponseEntity.ok(GastoMapper.toResponseDTO(gasto));
    }

    @GetMapping
    public ResponseEntity<List<GastoResponseDTO>> listarGastosActivos() {
        List<GastoResponseDTO> gastos = gastoService.listarGastosActivos().stream()
                .map(GastoMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<GastoResponseDTO>> listarPorEstado(@PathVariable("estado") Estado estado) {
        List<GastoResponseDTO> gastos = gastoService.listarGastosPorEstado(estado).stream()
                .map(GastoMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/tipo/{idTipoGasto}")
    public ResponseEntity<List<GastoResponseDTO>> listarPorTipo(@PathVariable("idTipoGasto") Long idTipoGasto) {
        List<GastoResponseDTO> gastos = gastoService.listarGastosPorTipo(idTipoGasto).stream()
                .map(GastoMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/metodo/{idMetodoPago}")
    public ResponseEntity<List<GastoResponseDTO>> listarPorMetodoPago(@PathVariable("idMetodoPago") Long idMetodoPago) {
        List<GastoResponseDTO> gastos = gastoService.listarGastosPorMetodoPago(idMetodoPago).stream()
                .map(GastoMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gastos);
    }

    @GetMapping("/descripcion/{descripcion}")
    public ResponseEntity<GastoResponseDTO> findByDescripcion(@PathVariable("descripcion") String descripcion) {
        Gasto gasto = gastoService.findByDescripcion(descripcion);
        return ResponseEntity.ok(GastoMapper.toResponseDTO(gasto));
    }
}

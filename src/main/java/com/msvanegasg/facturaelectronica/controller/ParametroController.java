package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ParametroDTO;
import com.msvanegasg.facturaelectronica.mapper.ParametroMapper;
import com.msvanegasg.facturaelectronica.models.Parametro;
import com.msvanegasg.facturaelectronica.service.ParametroService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    @Autowired
    private ParametroService parametroService;

    @GetMapping
    public ResponseEntity<List<Parametro>> findAll() {
    	List<Parametro> all = parametroService.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParametroDTO> getById(@PathVariable("id") Long id) {
        Parametro parametro = parametroService.findById(id);
        return ResponseEntity.ok(ParametroMapper.toDTO(parametro));
    }

    @PostMapping
    public ResponseEntity<ParametroDTO> create(@Valid @RequestBody ParametroDTO dto) {
        Parametro parametro = ParametroMapper.toEntity(dto);
        return ResponseEntity.ok(ParametroMapper.toDTO(parametroService.save(parametro)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametroDTO> update(@PathVariable("id") Long id, @Valid @RequestBody ParametroDTO dto) {
        Parametro existing = parametroService.findById(id);

        Parametro parametroToUpdate = ParametroMapper.toEntity(dto);
        parametroToUpdate.setIdParametro(existing.getIdParametro());
        parametroToUpdate.setActivo(existing.getActivo());

        return ResponseEntity.ok(ParametroMapper.toDTO(parametroService.save(parametroToUpdate)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        parametroService.disableById(id);
        return ResponseEntity.noContent().build();
    }
}

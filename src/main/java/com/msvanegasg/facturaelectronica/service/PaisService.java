package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.DTO.PaisDTO;
import com.msvanegasg.facturaelectronica.exception.PaisNotFoundException;
import com.msvanegasg.facturaelectronica.mapper.PaisMapper;
import com.msvanegasg.facturaelectronica.models.Pais;
import com.msvanegasg.facturaelectronica.repository.PaisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaisService {

    @Autowired
    private PaisRepository paisRepository;
    
    
    public List<Pais> findAll() {
        return paisRepository.findAll();
    }
    
    public List<Pais> findActive() {
        return paisRepository.findByActivoTrue();
    }
    
    public List<Pais> findActiveFalse() {
        return paisRepository.findByActivoFalse();
    }

    public Pais findById(String codigoPais) {
        return paisRepository.findById(codigoPais)
                .orElseThrow(() -> new PaisNotFoundException(codigoPais));
    }


    public PaisDTO save(PaisDTO dto) {
        Pais pais = PaisMapper.toEntity(dto);
        return PaisMapper.toDTO(paisRepository.save(pais));
    }
    
    public PaisDTO update(String codigoPais, PaisDTO paisDTO) {
        Pais paisExistente = paisRepository.findById(codigoPais)
                .orElseThrow(() -> new PaisNotFoundException(codigoPais));

        paisExistente.setNombre(paisDTO.getNombre());
        paisExistente.setMoneda(paisDTO.getMoneda());

        return PaisMapper.toDTO(paisRepository.save(paisExistente));
    }

    public void disableById(String codigoPais) {
        Pais pais = paisRepository.findById(codigoPais)
                .orElseThrow(() -> new PaisNotFoundException(codigoPais));
        pais.setActivo(false);
        paisRepository.save(pais);
    }
    
    public void activarPais(String codigoPais) {
        Pais pais = paisRepository.findById(codigoPais)
                .orElseThrow(() -> new PaisNotFoundException(codigoPais));

        if (!pais.getActivo()) {
        	pais.setActivo(true);
        	paisRepository.save(pais);
        }
    }
}

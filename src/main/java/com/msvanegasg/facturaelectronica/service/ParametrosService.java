package com.msvanegasg.facturaelectronica.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.models.Parametros;
import com.msvanegasg.facturaelectronica.repository.ParametrosRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ParametrosService {

    @Autowired
    private ParametrosRepository parametrosRepository;

    public List<Parametros> findAll() {
        return parametrosRepository.findAll();
    }

    public Optional<Parametros> findById(Long id) {
        return parametrosRepository.findById(id);
    }

    public Parametros save(Parametros parametros) {
        return parametrosRepository.save(parametros);
    }

    public void disableById(Long id) {
    	parametrosRepository.findById(id).ifPresent(parametros -> {
    		parametros.setActivo(false);
    		parametrosRepository.save(parametros);
        });
    }
}


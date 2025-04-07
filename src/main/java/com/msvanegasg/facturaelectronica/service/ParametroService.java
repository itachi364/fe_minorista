package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.exception.ParametroNotFoundException;
import com.msvanegasg.facturaelectronica.models.Parametro;
import com.msvanegasg.facturaelectronica.repository.ParametroRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParametroService {

    @Autowired
    private ParametroRepository parametroRepository;

    public List<Parametro> findAll() {
        return parametroRepository.findAll();
    }

    public Parametro findById(Long id) {
        return parametroRepository.findById(id)
                .orElseThrow(() -> new ParametroNotFoundException(id));
    }

    public Parametro save(Parametro parametro) {
        return parametroRepository.save(parametro);
    }

    public void disableById(Long id) {
        Parametro parametro = parametroRepository.findById(id)
                .orElseThrow(() -> new ParametroNotFoundException(id));

        parametro.setActivo(false);
        parametroRepository.save(parametro);
    }
}

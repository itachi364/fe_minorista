package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParametroRepository extends JpaRepository<Parametro, Long> {

    List<Parametro> findByActivoTrue();
    
    List<Parametro> findByActivoFalse();
}

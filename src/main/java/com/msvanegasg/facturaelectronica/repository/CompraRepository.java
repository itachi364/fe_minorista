package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.msvanegasg.facturaelectronica.enums.Estado;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByActivoTrue();
    
    List<Compra> findByActivoFalse();
    
    List<Compra> findByEstado(Estado estado);


}
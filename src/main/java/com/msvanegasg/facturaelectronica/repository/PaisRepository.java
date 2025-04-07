package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Pais;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaisRepository extends JpaRepository<Pais, String> {
	
    List<Pais> findByNombreContainingIgnoreCase(String nombre);
    
    List<Pais> findByActivoTrue();

    boolean existsByNombreIgnoreCase(String nombre);
}

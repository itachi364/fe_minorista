package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Cliente;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
	
	Optional<Cliente> findByNumeroDocumento(Long numeroDocumento);
	
	List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    List<Cliente> findByActivoTrue();
    
    boolean existsByNumeroDocumento(Long numeroDocumento);
	
}

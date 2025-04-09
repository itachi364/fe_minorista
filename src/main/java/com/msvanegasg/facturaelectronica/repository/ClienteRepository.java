package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
	
	Optional<Cliente> findByNumeroDocumento(Long numeroDocumento);
	
	Optional<Cliente> findByNumeroDocumentoAndTipoDocumento(Long numeroDocumento, TipoDocumento tipoDocumento);
	
	Cliente findByNombreContainingIgnoreCase(String nombre);

    List<Cliente> findByActivoTrue();
    
    List<Cliente> findByActivoFalse();
    
    boolean existsByNumeroDocumentoAndTipoDocumento_Codigo(Long numeroDocumento, Long tipoDocumentoCodigo);
	
}

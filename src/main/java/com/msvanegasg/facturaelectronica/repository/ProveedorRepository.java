package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
	
	Optional<Proveedor> findByNumeroDocumentoAndTipoDocumento(Long numeroDocumento, TipoDocumento tipoDocumento);

    Proveedor findByNombreContainingIgnoreCase(String nombre);

    List<Proveedor> findByActivoTrue();
    
    List<Proveedor> findByActivoFalse();

    boolean existsByNumeroDocumento(Long numeroDocumento);
}

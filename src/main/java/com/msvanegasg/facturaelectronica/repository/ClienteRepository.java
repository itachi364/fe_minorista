package com.msvanegasg.facturaelectronica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.msvanegasg.facturaelectronica.models.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	Optional<Cliente> findByIdTipoDocumentoAndNumeroDocumento(Long idTipoDocumento, Long numeroDocumento);

	List<Cliente> findAllByActivo(Boolean activo);

	@Query("SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
	List<Cliente> findByNombreContainingIgnoreCase(String nombre);

	boolean existsByNumeroDocumentoAndIdTipoDocumento(Long numeroDocumento, Long idTipoDocumento);

}

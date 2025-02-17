package com.msvanegasg.facturaelectronica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.msvanegasg.facturaelectronica.models.Proveedor;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
}
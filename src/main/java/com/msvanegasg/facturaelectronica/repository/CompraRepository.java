package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByProveedorIdProveedor(Long idProveedor);

    List<Compra> findByActivoTrue();

}
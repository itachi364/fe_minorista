package com.msvanegasg.facturaelectronica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.msvanegasg.facturaelectronica.models.MetodoPago;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
}
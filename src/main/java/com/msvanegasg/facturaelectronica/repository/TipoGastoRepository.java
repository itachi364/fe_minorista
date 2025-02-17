package com.msvanegasg.facturaelectronica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.msvanegasg.facturaelectronica.models.TipoGasto;

@Repository
public interface TipoGastoRepository extends JpaRepository<TipoGasto, Long> {

}

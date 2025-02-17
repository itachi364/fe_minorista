package com.msvanegasg.facturaelectronica.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.msvanegasg.facturaelectronica.models.Parametros;

@Repository
public interface ParametrosRepository extends JpaRepository<Parametros, Long> {
}
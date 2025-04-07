package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.RegistroAccesos;
import com.msvanegasg.facturaelectronica.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroAccesosRepository extends JpaRepository<RegistroAccesos, Long> {

    List<RegistroAccesos> findByUsuario(Usuario usuario);

    List<RegistroAccesos> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    List<RegistroAccesos> findByDireccionIp(String direccionIp);

    List<RegistroAccesos> findByAccionContainingIgnoreCase(String accion);
}

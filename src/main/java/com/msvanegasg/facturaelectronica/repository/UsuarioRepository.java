package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoElectronicoIgnoreCase(String correoElectronico);

    Optional<Usuario> findByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByCorreoElectronicoIgnoreCase(String correoElectronico);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    List<Usuario> findByEstadoIgnoreCase(String estado);
}

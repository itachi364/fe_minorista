package com.msvanegasg.facturaelectronica.repository;

import com.msvanegasg.facturaelectronica.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    //List<Categoria> findByNombreContainingIgnoreCase(String nombre);
	@Query(value = "SELECT * FROM categoria WHERE unaccent(lower(nombre)) LIKE concat('%', unaccent(lower(:nombre)), '%')", nativeQuery = true)
	List<Categoria> findByNombreIgnoreCaseAndAccent(@Param("nombre") String nombre);

    List<Categoria> findByActivoTrue();
    
    List<Categoria> findByActivoFalse();
}

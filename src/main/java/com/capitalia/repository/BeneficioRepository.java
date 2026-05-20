package com.capitalia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Beneficio;

@Repository
public interface BeneficioRepository extends JpaRepository<Beneficio, Integer> {

    // Consulta nativa para unir las tablas y sacar los beneficios de un nivel específico
    @Query(value = "SELECT b.* FROM beneficio b " +
                   "JOIN detalle_nivel_beneficio d ON b.id_beneficio = d.id_beneficio " +
                   "WHERE d.id_nivel = :idNivel", nativeQuery = true)
    List<Beneficio> findBeneficiosPorNivel(@Param("idNivel") Integer idNivel);
}
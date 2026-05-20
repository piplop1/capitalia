package com.capitalia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Configuracion;

@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {
    // Buscamos por clave (String) en lugar de ID entero
}
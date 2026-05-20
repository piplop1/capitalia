package com.capitalia.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Auditoria;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Integer> {
    // Métodos de búsqueda si quieres ver el log luego
}
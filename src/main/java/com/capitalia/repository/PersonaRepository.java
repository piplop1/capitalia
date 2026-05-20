package com.capitalia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    // Métodos para persona (DNI, etc.) si los necesitaras en el futuro
}
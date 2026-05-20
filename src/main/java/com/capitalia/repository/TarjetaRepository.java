package com.capitalia.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Tarjeta;

@Repository
public interface TarjetaRepository extends JpaRepository<Tarjeta, Integer> {
}
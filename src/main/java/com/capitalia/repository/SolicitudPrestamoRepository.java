package com.capitalia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.SolicitudPrestamo;

@Repository
public interface SolicitudPrestamoRepository extends JpaRepository<SolicitudPrestamo, Integer> {
    
    // Listar para las tablas
    List<SolicitudPrestamo> findByEstado(String estado);
    List<SolicitudPrestamo> findByUsuario_Id(Integer idUsuario);

    // --- NUEVO: Para los gráficos del Dashboard (ESTO TE FALTA) ---
    long countByEstado(String estado);
}
package com.capitalia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Prestamo;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {
    
    // Busca los préstamos de un usuario
    List<Prestamo> findByCuentaDesembolso_Usuario_Id(Integer idUsuario);

    // Cuenta cuántos préstamos hay por estado (Para el gráfico de reportes)
    long countByEstadoPrestamo(String estadoPrestamo); 
}
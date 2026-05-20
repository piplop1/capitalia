package com.capitalia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.CronogramaPago;

@Repository
public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, Integer> {
    // Busca cronograma de un préstamo específico (ya lo tenías)
    List<CronogramaPago> findByPrestamo_IdPrestamoOrderByNumeroCuotaAsc(Integer idPrestamo);

    // --- NUEVO: Busca TODAS las cuotas de TODOS los préstamos de un usuario ---

    List<CronogramaPago> findByPrestamo_IdPrestamoAndEstadoCuota(Integer idPrestamo, String estado);

    List<CronogramaPago> findByPrestamo_CuentaDesembolso_Usuario_IdOrderByPrestamo_FechaDesembolsoDescFechaVencimientoAsc(Integer usuarioId);
}
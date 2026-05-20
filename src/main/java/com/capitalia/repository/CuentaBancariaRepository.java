package com.capitalia.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository; // Importante importar esto
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.capitalia.model.CuentaBancaria;

@Repository
public interface CuentaBancariaRepository extends JpaRepository<CuentaBancaria, Integer> {
    
    Optional<CuentaBancaria> findByUsuario_Id(Integer idUsuario);

    // --- NUEVO: Para sumar todo el dinero del banco ---
    @Query("SELECT SUM(c.saldoActual) FROM CuentaBancaria c")
    BigDecimal sumarSaldoTotal();
}
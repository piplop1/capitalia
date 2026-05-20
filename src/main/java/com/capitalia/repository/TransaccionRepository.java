package com.capitalia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Transaccion;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {
    
    // ESTA ES LA LÍNEA QUE TE FALTA:
    // Busca transacciones donde el campo 'cuentaBancaria' tenga el 'idCuenta' específico
    List<Transaccion> findByCuentaBancaria_IdCuenta(Integer idCuenta);
}
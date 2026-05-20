package com.capitalia.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitalia.model.CuentaBancaria;
import com.capitalia.model.Transaccion;
import com.capitalia.repository.CuentaBancariaRepository;
import com.capitalia.repository.TransaccionRepository;

@Service
public class FinanzasService {

    @Autowired
    private CuentaBancariaRepository cuentaRepository;

    @Autowired
    private TransaccionRepository transaccionRepository;

    // La anotación @Transactional asegura que si algo falla, no se descuenta el dinero
    @Transactional
    public void realizarOperacion(Integer idUsuario, String tipo, BigDecimal monto, String descripcion) throws Exception {
        
        // 1. Buscar la cuenta del usuario
        CuentaBancaria cuenta = cuentaRepository.findByUsuario_Id(idUsuario)
                .orElseThrow(() -> new Exception("Usuario no tiene cuenta asignada."));

        // 2. Validar saldo si es retiro
        if ("RETIRO".equals(tipo) && cuenta.getSaldoActual().compareTo(monto) < 0) {
            throw new Exception("Saldo insuficiente para realizar el retiro.");
        }

        // 3. Actualizar el saldo de la cuenta
        if ("DEPOSITO".equals(tipo)) {
            cuenta.setSaldoActual(cuenta.getSaldoActual().add(monto));
        } else if ("RETIRO".equals(tipo)) {
            cuenta.setSaldoActual(cuenta.getSaldoActual().subtract(monto));
        }

        // 4. Crear el registro de la transacción
        Transaccion nuevaTransaccion = new Transaccion();
        nuevaTransaccion.setCuentaBancaria(cuenta);
        nuevaTransaccion.setTipoMovimiento(tipo); // Asegúrate que tu ENUM o String coincida con la BD
        nuevaTransaccion.setMonto(monto);
        nuevaTransaccion.setDescripcion(descripcion);
        nuevaTransaccion.setFechaTransaccion(LocalDateTime.now());

        // 5. Guardar ambos cambios
        cuentaRepository.save(cuenta);       // Actualiza saldo
        transaccionRepository.save(nuevaTransaccion); // Guarda historial
    }
}
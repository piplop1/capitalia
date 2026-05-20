package com.capitalia.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.capitalia.model.*;
import com.capitalia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestamoService {

    @Autowired
    private SolicitudPrestamoRepository solicitudRepository;

    @Autowired
    private CuentaBancariaRepository cuentaRepository;
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private ConfiguracionRepository configRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private CronogramaPagoRepository cronogramaRepository;

    // =======================================================
    // 1. PROCESAR SOLICITUD (ADMINISTRADOR)
    // Aprueba, desembolsa, crea la deuda y genera el cronograma.
    // =======================================================
    @Transactional
    public void procesarSolicitud(Integer idSolicitud, String accion) throws Exception {
        SolicitudPrestamo solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new Exception("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new Exception("Esta solicitud ya fue procesada anteriormente.");
        }

        if ("APROBAR".equals(accion)) {
            // A. Cambiar estado a APROBADO
            solicitud.setEstado("APROBADO");
            
            // B. Buscar cuenta del usuario
            CuentaBancaria cuenta = cuentaRepository.findByUsuario_Id(solicitud.getUsuario().getId())
                    .orElseThrow(() -> new Exception("El usuario no tiene cuenta activa para desembolsar."));
            
            // C. Desembolsar dinero (Sumar al saldo)
            cuenta.setSaldoActual(cuenta.getSaldoActual().add(solicitud.getMontoSolicitado()));
            cuentaRepository.save(cuenta);

            // D. Registrar la transacción en el historial
            Transaccion transaccion = new Transaccion();
            transaccion.setCuentaBancaria(cuenta);
            transaccion.setTipoMovimiento("DESEMBOLSO");
            transaccion.setMonto(solicitud.getMontoSolicitado());
            transaccion.setDescripcion("Préstamo Aprobado #" + solicitud.getIdSolicitud());
            transaccion.setFechaTransaccion(LocalDateTime.now());
            transaccionRepository.save(transaccion);

            // E. Crear el contrato de deuda (Tabla PRESTAMO)
            Prestamo nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setSolicitud(solicitud);
            nuevoPrestamo.setCuentaDesembolso(cuenta);
            nuevoPrestamo.setMontoAprobado(solicitud.getMontoSolicitado());
            BigDecimal tasa = configRepository.findById("TASA_INTERES")
                    .map(Configuracion::getValor)
                    .orElse(new BigDecimal("0.15"));
            nuevoPrestamo.setTasaInteresAplicada(tasa);
            nuevoPrestamo.setFechaDesembolso(LocalDateTime.now());
            nuevoPrestamo.setEstadoPrestamo("ACTIVO"); // La deuda nace Activa
            
            prestamoRepository.save(nuevoPrestamo);

            // F. GENERAR CRONOGRAMA (Llama al método privado de abajo)
            generarCronograma(nuevoPrestamo, solicitud.getPlazoMeses(), solicitud.getMontoSolicitado());

            // G. Enviar correo de notificación
            try {
                emailService.sendLoanApprovalEmail(
                    solicitud.getUsuario().getEmail(), 
                    solicitud.getUsuario().getFullname(), 
                    solicitud.getMontoSolicitado().toString()
                );
            } catch (Exception e) {
                System.err.println("Error enviando correo: " + e.getMessage());
            }

        } else if ("RECHAZAR".equals(accion)) {
            solicitud.setEstado("RECHAZADO");
        }

        solicitudRepository.save(solicitud);
    }

    // =======================================================
    // 2. MÉTODO AUXILIAR: GENERAR CUOTAS
    // Crea las filas en la tabla cronograma_pago
    // =======================================================
    private void generarCronograma(Prestamo prestamo, int cuotas, BigDecimal montoOriginal) {
        BigDecimal tasa = prestamo.getTasaInteresAplicada(); // Debe coincidir con la tasa del préstamo

        BigDecimal totalPagar = montoOriginal.multiply(BigDecimal.ONE.add(tasa));
        BigDecimal montoCuota = totalPagar.divide(new BigDecimal(cuotas), 2, RoundingMode.HALF_UP);
        BigDecimal capitalCuota = montoOriginal.divide(new BigDecimal(cuotas), 2, RoundingMode.HALF_UP);
        BigDecimal interesCuota = montoCuota.subtract(capitalCuota);

        // Calcular diferencia de redondeo
        BigDecimal totalCuotas = montoCuota.multiply(new BigDecimal(cuotas));
        BigDecimal diferencia = totalPagar.subtract(totalCuotas);

        for (int i = 1; i <= cuotas; i++) {
            CronogramaPago pago = new CronogramaPago();
            pago.setPrestamo(prestamo);
            pago.setNumeroCuota(i);
            pago.setFechaVencimiento(LocalDate.now().plusMonths(i));
            pago.setMontoCapital(capitalCuota);
            pago.setMontoInteres(interesCuota);

            // La última cuota absorbe la diferencia de redondeo
            BigDecimal montoCuotaFinal = (i == cuotas)
                    ? montoCuota.add(diferencia)
                    : montoCuota;

            pago.setMontoTotalCuota(montoCuotaFinal);
            pago.setEstadoCuota("PENDIENTE");
            cronogramaRepository.save(pago);
        }
    }

    // =======================================================
    // 3. PAGAR PRÉSTAMO (USUARIO)
    // Cobra el saldo total y cierra el préstamo.
    // =======================================================
    @Transactional
    public void pagarPrestamo(Integer idPrestamo) throws Exception {
        // A. Buscar la deuda
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() -> new Exception("Préstamo no encontrado"));

        if (!"ACTIVO".equals(prestamo.getEstadoPrestamo())) {
            throw new Exception("Este préstamo no está activo o ya fue pagado.");
        }

        // B. Suma solo las cuotas que aún están pendientes
        BigDecimal montoTotal = cronogramaRepository
                .findByPrestamo_IdPrestamoAndEstadoCuota(prestamo.getIdPrestamo(), "PENDIENTE")
                .stream()
                .map(CronogramaPago::getMontoTotalCuota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // C. Verificar saldo del usuario
        CuentaBancaria cuenta = prestamo.getCuentaDesembolso();
        if (cuenta.getSaldoActual().compareTo(montoTotal) < 0) {
            throw new Exception("Saldo insuficiente. Necesitas S/ " + montoTotal + " para pagar.");
        }

        // D. Cobrar (Restar saldo)
        cuenta.setSaldoActual(cuenta.getSaldoActual().subtract(montoTotal));
        cuentaRepository.save(cuenta);

        // E. Cerrar deuda (Cambiar estado a PAGADO)
        prestamo.setEstadoPrestamo("PAGADO");
        prestamoRepository.save(prestamo);

        // F. Actualizar todas las cuotas del cronograma a PAGADO
        // (Esto es opcional pero recomendado para que el cronograma se vea limpio)
        List<CronogramaPago> cuotasPendientes = cronogramaRepository
                .findByPrestamo_IdPrestamoAndEstadoCuota(prestamo.getIdPrestamo(), "PENDIENTE");
        for (CronogramaPago cuota : cuotasPendientes) {
            cuota.setEstadoCuota("PAGADO");
            cuota.setFechaPagoReal(LocalDate.now());
            cronogramaRepository.save(cuota);
        }

        // G. Registrar transacción de pago
        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaBancaria(cuenta);
        transaccion.setTipoMovimiento("PAGO_PRESTAMO");
        transaccion.setMonto(montoTotal);
        transaccion.setDescripcion("Pago total de préstamo #" + prestamo.getIdPrestamo());
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccionRepository.save(transaccion);
    }
    @Transactional
    public void pagarCuota(Integer idCronograma) throws Exception {
        CronogramaPago cuota = cronogramaRepository.findById(idCronograma)
                .orElseThrow(() -> new Exception("Cuota no encontrada"));

        if ("PAGADO".equals(cuota.getEstadoCuota())) {
            throw new Exception("Esta cuota ya fue pagada.");
        }

        Prestamo prestamo = cuota.getPrestamo();
        if (!"ACTIVO".equals(prestamo.getEstadoPrestamo())) {
            throw new Exception("Este préstamo no está activo.");
        }

        CuentaBancaria cuenta = prestamo.getCuentaDesembolso();
        BigDecimal montoCuota = cuota.getMontoTotalCuota();

        if (cuenta.getSaldoActual().compareTo(montoCuota) < 0) {
            throw new Exception("Saldo insuficiente. Necesitás S/ " + montoCuota + " para pagar esta cuota.");
        }

        // Verificar que no haya cuotas anteriores pendientes
        boolean hayAnterioresPendientes = cronogramaRepository
                .findByPrestamo_IdPrestamoAndEstadoCuota(prestamo.getIdPrestamo(), "PENDIENTE")
                .stream()
                .anyMatch(c -> c.getNumeroCuota() < cuota.getNumeroCuota());

        if (hayAnterioresPendientes) {
            throw new Exception("Debés pagar las cuotas anteriores primero.");
        }
        
        // Descontar saldo
        cuenta.setSaldoActual(cuenta.getSaldoActual().subtract(montoCuota));
        cuentaRepository.save(cuenta);

        // Marcar cuota como pagada
        cuota.setEstadoCuota("PAGADO");
        cuota.setFechaPagoReal(LocalDate.now());
        cronogramaRepository.save(cuota);

        // Registrar transacción
        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaBancaria(cuenta);
        transaccion.setTipoMovimiento("PAGO_CUOTA");
        transaccion.setMonto(montoCuota);
        transaccion.setDescripcion("Pago cuota #" + cuota.getNumeroCuota() + " de préstamo #" + prestamo.getIdPrestamo());
        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccionRepository.save(transaccion);

        // Verificar si todas las cuotas están pagadas → cerrar préstamo
        long cuotasPendientes = cronogramaRepository
                .findByPrestamo_IdPrestamoAndEstadoCuota(prestamo.getIdPrestamo(), "PENDIENTE")
                .stream().count();

        if (cuotasPendientes == 0) {
            prestamo.setEstadoPrestamo("PAGADO");
            prestamoRepository.save(prestamo);
        }
    }
}
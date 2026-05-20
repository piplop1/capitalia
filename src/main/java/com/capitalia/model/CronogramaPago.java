package com.capitalia.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cronograma_pago")
public class CronogramaPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCronograma;

    @ManyToOne
    @JoinColumn(name = "id_prestamo")
    private Prestamo prestamo;

    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal montoCapital;
    private BigDecimal montoInteres;
    private BigDecimal montoTotalCuota;
    private String estadoCuota; // 'PENDIENTE', 'PAGADO', 'VENCIDO'
    private LocalDate fechaPagoReal;

    // Getters y Setters
    public Integer getIdCronograma() { return idCronograma; }
    public void setIdCronograma(Integer idCronograma) { this.idCronograma = idCronograma; }
    public Prestamo getPrestamo() { return prestamo; }
    public void setPrestamo(Prestamo prestamo) { this.prestamo = prestamo; }
    public Integer getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(Integer numeroCuota) { this.numeroCuota = numeroCuota; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public BigDecimal getMontoCapital() { return montoCapital; }
    public void setMontoCapital(BigDecimal montoCapital) { this.montoCapital = montoCapital; }
    public BigDecimal getMontoInteres() { return montoInteres; }
    public void setMontoInteres(BigDecimal montoInteres) { this.montoInteres = montoInteres; }
    public BigDecimal getMontoTotalCuota() { return montoTotalCuota; }
    public void setMontoTotalCuota(BigDecimal montoTotalCuota) { this.montoTotalCuota = montoTotalCuota; }
    public String getEstadoCuota() { return estadoCuota; }
    public void setEstadoCuota(String estadoCuota) { this.estadoCuota = estadoCuota; }
    public LocalDate getFechaPagoReal() { return fechaPagoReal; }
    public void setFechaPagoReal(LocalDate fechaPagoReal) { this.fechaPagoReal = fechaPagoReal; }
}
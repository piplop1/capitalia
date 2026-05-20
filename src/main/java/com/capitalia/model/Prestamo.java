package com.capitalia.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "prestamo")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Integer idPrestamo;

    @OneToOne
    @JoinColumn(name = "id_solicitud")
    private SolicitudPrestamo solicitud;

    @ManyToOne
    @JoinColumn(name = "id_cuenta_desembolso")
    private CuentaBancaria cuentaDesembolso;

    @Column(name = "monto_aprobado")
    private BigDecimal montoAprobado;

    @Column(name = "tasa_interes_aplicada")
    private BigDecimal tasaInteresAplicada;

    @Column(name = "fecha_desembolso")
    private LocalDateTime fechaDesembolso;

    @Column(name = "estado_prestamo")
    private String estadoPrestamo; // 'ACTIVO', 'PAGADO', 'MORA'

    // Constructor vacío
    public Prestamo() {}

    // Getters y Setters
    public Integer getIdPrestamo() { return idPrestamo; }
    public void setIdPrestamo(Integer idPrestamo) { this.idPrestamo = idPrestamo; }

    public SolicitudPrestamo getSolicitud() { return solicitud; }
    public void setSolicitud(SolicitudPrestamo solicitud) { this.solicitud = solicitud; }

    public CuentaBancaria getCuentaDesembolso() { return cuentaDesembolso; }
    public void setCuentaDesembolso(CuentaBancaria cuentaDesembolso) { this.cuentaDesembolso = cuentaDesembolso; }

    public BigDecimal getMontoAprobado() { return montoAprobado; }
    public void setMontoAprobado(BigDecimal montoAprobado) { this.montoAprobado = montoAprobado; }

    public BigDecimal getTasaInteresAplicada() { return tasaInteresAplicada; }
    public void setTasaInteresAplicada(BigDecimal tasaInteresAplicada) { this.tasaInteresAplicada = tasaInteresAplicada; }

    public LocalDateTime getFechaDesembolso() { return fechaDesembolso; }
    public void setFechaDesembolso(LocalDateTime fechaDesembolso) { this.fechaDesembolso = fechaDesembolso; }

    public String getEstadoPrestamo() { return estadoPrestamo; }
    public void setEstadoPrestamo(String estadoPrestamo) { this.estadoPrestamo = estadoPrestamo; }
}
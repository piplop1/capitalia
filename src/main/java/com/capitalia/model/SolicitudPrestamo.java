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
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitud_prestamo")
public class SolicitudPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    // Relación con el Usuario que pide el dinero
    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "monto_solicitado")
    private BigDecimal montoSolicitado;

    @Column(name = "plazo_meses")
    private Integer plazoMeses;

    @Column(name = "estado")
    private String estado; // 'PENDIENTE', 'APROBADO', 'RECHAZADO'

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    public SolicitudPrestamo() {}

    // Getters y Setters
    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public BigDecimal getMontoSolicitado() { return montoSolicitado; }
    public void setMontoSolicitado(BigDecimal montoSolicitado) { this.montoSolicitado = montoSolicitado; }

    public Integer getPlazoMeses() { return plazoMeses; }
    public void setPlazoMeses(Integer plazoMeses) { this.plazoMeses = plazoMeses; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
}
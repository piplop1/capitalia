package com.capitalia.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAuditoria;
    
    @Column(name = "usuario_responsable")
    private String usuarioResponsable;
    
    private String accion;
    private String detalle;
    private LocalDateTime fecha;

    // Constructor vacío (Obligatorio para JPA)
    public Auditoria() {}
    
    // Constructor para guardar rápido
    public Auditoria(String usuario, String accion, String detalle) {
        this.usuarioResponsable = usuario;
        this.accion = accion;
        this.detalle = detalle;
        this.fecha = LocalDateTime.now();
    }

    // --- GETTERS Y SETTERS (Esto solucionará las advertencias) ---

    public Integer getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Integer idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public void setUsuarioResponsable(String usuarioResponsable) {
        this.usuarioResponsable = usuarioResponsable;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
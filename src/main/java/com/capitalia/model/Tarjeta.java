package com.capitalia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tarjeta")
public class Tarjeta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idTarjeta;

    @Column(name = "numero_tarjeta")
    private String numeroTarjeta;

    @Column(name = "fecha_vencimiento")
    private String fechaVencimiento;

    @ManyToOne
    @JoinColumn(name = "id_cuenta")
    private CuentaBancaria cuenta;

    // Getters y Setters
    public Integer getIdTarjeta() { return idTarjeta; }
    public void setIdTarjeta(Integer idTarjeta) { this.idTarjeta = idTarjeta; }
    
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }
    
    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public CuentaBancaria getCuenta() { return cuenta; }
    public void setCuenta(CuentaBancaria cuenta) { this.cuenta = cuenta; }
}
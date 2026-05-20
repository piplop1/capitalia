package com.capitalia.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cuenta_bancaria")
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Integer idCuenta;

    @Column(name = "numero_cuenta")
    private String numeroCuenta;

    @Column(name = "saldo_actual")
    private BigDecimal saldoActual;

    // --- NUEVO CAMPO: LÍMITE DIARIO ---
    // Este campo es necesario para la funcionalidad de "Configuraciones"
    @Column(name = "limite_diario")
    private BigDecimal limiteDiario;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
    
    // --- CONSTRUCTORES ---
    public CuentaBancaria() {}

    // --- GETTERS Y SETTERS ---

    public Integer getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(Integer idCuenta) {
        this.idCuenta = idCuenta;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
    }

    // Getters y Setters para el nuevo campo
    public BigDecimal getLimiteDiario() {
        return limiteDiario;
    }

    public void setLimiteDiario(BigDecimal limiteDiario) {
        this.limiteDiario = limiteDiario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
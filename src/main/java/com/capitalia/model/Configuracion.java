package com.capitalia.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @Column(name = "clave")
    private String clave;

    @Column(name = "valor")
    private BigDecimal valor;

    @Column(name = "descripcion")
    private String descripcion;

    public Configuracion() {}

    // Getters y Setters
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
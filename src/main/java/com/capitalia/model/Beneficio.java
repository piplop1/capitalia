package com.capitalia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "beneficio")
public class Beneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficio")
    private Integer idBeneficio;

    @Column(name = "nombre_beneficio")
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    // Getters y Setters
    public Integer getIdBeneficio() { return idBeneficio; }
    public void setIdBeneficio(Integer idBeneficio) { this.idBeneficio = idBeneficio; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
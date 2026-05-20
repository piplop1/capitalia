package com.capitalia.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // "usuario" o "administrador"

    // --- NUEVO: Relación con la tabla Persona ---
    // Esto conecta el campo 'id_persona' de la tabla usuario con la entidad Persona
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_persona", referencedColumnName = "id_persona")
    private Persona persona;

    // --- NUEVO: Fecha de creación para saber antigüedad ---
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "activo")
    private Boolean activo = true;

    // Campos para el reseteo de contraseña
    private String resetPasswordCode;
    private LocalDateTime resetPasswordCodeExpires;

    // Constructor vacío (requerido por JPA)
    public Usuario() {
    }

    // Constructor para crear usuarios fácilmente
    public Usuario(String fullname, String email, String password, String role) {
        this.fullname = fullname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.fechaCreacion = LocalDateTime.now(); // Asigna la fecha actual al crear
        this.activo = true;
    }

    // --- Getters y Setters ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Getters y Setters NUEVOS para Persona
    public Persona getPersona() {
        return persona;
    }

    public void setPersona(Persona persona) {
        this.persona = persona;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getResetPasswordCode() {
        return resetPasswordCode;
    }

    public void setResetPasswordCode(String resetPasswordCode) {
        this.resetPasswordCode = resetPasswordCode;
    }

    public LocalDateTime getResetPasswordCodeExpires() {
        return resetPasswordCodeExpires;
    }

    public void setResetPasswordCodeExpires(LocalDateTime resetPasswordCodeExpires) {
        this.resetPasswordCodeExpires = resetPasswordCodeExpires;
    }
}
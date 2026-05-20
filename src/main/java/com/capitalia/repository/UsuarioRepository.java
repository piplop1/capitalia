package com.capitalia.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capitalia.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmail(String email);

    // --- NUEVO: Buscar por el código de recuperación ---
    Optional<Usuario> findByResetPasswordCode(String resetPasswordCode);

    List<Usuario> findAllByRole(String role);
}
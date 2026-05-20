package com.capitalia.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.capitalia.model.Usuario;
import com.capitalia.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Inyectamos el encriptador de Spring Security
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        // 1. Encriptamos la contraseña antes de guardar
        String passEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passEncriptada);

        // 2. Guardamos en la BD y retornamos el usuario (ya con su ID generado)
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void actualizarNombre(Integer usuarioId, String nuevoNombre) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con id: " + usuarioId));
        
        usuario.setFullname(nuevoNombre);
        usuarioRepository.save(usuario);
    }
}
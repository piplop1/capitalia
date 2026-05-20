package com.capitalia.service;

import com.capitalia.model.Usuario;

public interface IUsuarioService {
    // Ahora devuelve el Usuario guardado (con ID) en lugar de un String
    Usuario guardarUsuario(Usuario usuario);
    
    void actualizarNombre(Integer usuarioId, String nuevoNombre);
}
package com.capitalia.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar esto
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.capitalia.model.Usuario;
import com.capitalia.repository.UsuarioRepository;
import com.capitalia.service.EmailService;

@RestController
@RequestMapping("/api/password") // Prefijo para ordenar las rutas (API REST para el Modal)
public class PasswordResetController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    // --- NUEVO: Inyectamos el encriptador ---
    @Autowired
    private PasswordEncoder passwordEncoder;

    // PASO 1: SOLICITAR CÓDIGO
    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (!usuarioOpt.isPresent()) {
            // Por seguridad, a veces es mejor no decir si el correo existe o no, 
            // pero para tu demo dejaremos el mensaje claro.
            return ResponseEntity.badRequest().body(Map.of("error", "Correo no registrado."));
        }

        Usuario usuario = usuarioOpt.get();
        // Generar código de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(999999));

        usuario.setResetPasswordCode(code);
        usuario.setResetPasswordCodeExpires(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        emailService.sendPasswordResetCode(email, usuario.getFullname(), code);

        return ResponseEntity.ok(Map.of("message", "Código enviado."));
    }

    // PASO 2: VERIFICAR CÓDIGO
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetPasswordCode(code);

        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Código inválido."));
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.getResetPasswordCodeExpires().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El código ha expirado."));
        }

        // Devolvemos el email para usarlo en el siguiente paso de forma segura
        return ResponseEntity.ok(Map.of("message", "Código correcto.", "email", usuario.getEmail()));
    }

    // PASO 3: CAMBIAR CONTRASEÑA (CON ENCRIPTACIÓN)
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String newPassword = payload.get("password");
        String code = payload.get("code"); // Doble verificación de seguridad

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error de usuario."));
        }

        Usuario usuario = usuarioOpt.get();
        
        // Verificación final de seguridad antes de guardar
        if (usuario.getResetPasswordCode() == null || !code.equals(usuario.getResetPasswordCode())) {
             return ResponseEntity.badRequest().body(Map.of("error", "Sesión inválida."));
        }

        // --- AQUÍ ESTÁ EL CAMBIO IMPORTANTE ---
        // Encriptamos la nueva contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(newPassword));
        // --------------------------------------

        usuario.setResetPasswordCode(null);
        usuario.setResetPasswordCodeExpires(null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada."));
    }
}
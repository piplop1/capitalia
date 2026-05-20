package com.capitalia.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.capitalia.model.Usuario;
import com.capitalia.service.FinanzasService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TransaccionController {

    @Autowired
    private FinanzasService finanzasService;

    @PostMapping("/transacciones/nueva")
    public String procesarTransaccion(
            @RequestParam("tipo") String tipo, // DEPOSITO o RETIRO
            @RequestParam("monto") BigDecimal monto,
            @RequestParam("descripcion") String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        try {
            // Llamamos al servicio que creamos en el paso 1
            finanzasService.realizarOperacion(usuario.getId(), tipo, monto, descripcion);
            
            redirectAttributes.addFlashAttribute("successMessage", "¡Operación exitosa!");
        } catch (Exception e) {
            // Si hay error (ej. saldo insuficiente), avisamos al usuario
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }

        // Volvemos a la página de servicios para ver el nuevo historial
        return "redirect:/serviciosUsuarios";
    }
}
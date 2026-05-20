package com.capitalia.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.capitalia.model.CuentaBancaria;
import com.capitalia.model.Usuario;
import com.capitalia.repository.CuentaBancariaRepository;
import com.capitalia.repository.UsuarioRepository;
import com.capitalia.service.EmailService;
import com.capitalia.service.IUsuarioService;

@Controller
@SessionAttributes("usuarioLogueado") // Mantiene al usuario en sesión
public class PanelGeneralController {

    @Autowired
    private IUsuarioService usuarioService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private CuentaBancariaRepository cuentaRepository;

    // --- SEGURIDAD: Inyectar el Encriptador ---
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. NAVEGACIÓN GENERAL
    // ==========================================

    @GetMapping("/inicio")
    public String inicioPage() {
        return "redirect:/";
    }

    @GetMapping("/nosotros")
    public String nosotrosPage() {return "public/nosotros";
    }

    @GetMapping("/servicios")
    public String serviciosPage() {
        return "public/servicios";
    }

    @GetMapping("/registro")
    public String registroPage() {
        return "auth/registro";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // ==========================================
    // 2. DASHBOARD DE USUARIOS
    // ==========================================
    @GetMapping("/dashboardUsuarios")
    public String dashboardUsuarios(Model model) {
        // Verificar sesión
        if (!model.containsAttribute("usuarioLogueado")) {
            return "redirect:/login"; 
        }
        
        Usuario usuario = (Usuario) model.getAttribute("usuarioLogueado");

        // Buscar cuenta y saldo
        Optional<CuentaBancaria> cuentaOpt = cuentaRepository.findByUsuario_Id(usuario.getId());

        if (cuentaOpt.isPresent()) {
            model.addAttribute("saldo", cuentaOpt.get().getSaldoActual());
            model.addAttribute("numeroCuenta", cuentaOpt.get().getNumeroCuenta());
            
            // Lógica visual de nivel (Básico por defecto en el dashboard)
            model.addAttribute("nivel", "Nivel Básico"); 
        } else {
            model.addAttribute("saldo", BigDecimal.ZERO);
            model.addAttribute("numeroCuenta", "Sin Asignar");
            model.addAttribute("nivel", "Sin Nivel");
        }

        return "usuario/dashboard";
    }


    // ==========================================
    // 3. REGISTRO (Con Encriptación y Cuenta Automática)
    // ==========================================
    @PostMapping("/registro")
    public String handleRegistration(@RequestParam("fullname") String fullname, 
                                     @RequestParam("email") String email,
                                     @RequestParam("password") String password,
                                     RedirectAttributes redirectAttributes) {
        
        // A. Validar correo duplicado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "El correo electrónico ya está registrado.");
            return "redirect:/registro";
        }
        
        // B. Crear Usuario
        Usuario nuevoUsuario = new Usuario(fullname, email, password, "usuario");
        
        // El servicio se encarga de encriptar la contraseña antes de guardar
        Usuario usuarioGuardado = usuarioService.guardarUsuario(nuevoUsuario); 

        // C. Crear Cuenta Bancaria Automática
        if (usuarioGuardado != null) {
            try {
                CuentaBancaria nuevaCuenta = new CuentaBancaria();
                nuevaCuenta.setUsuario(usuarioGuardado);
                
                // Generar número de cuenta único (Ej: CTA-1716253...)
                nuevaCuenta.setNumeroCuenta("CTA-" + System.currentTimeMillis()); 
                
                // Saldo inicial
                nuevaCuenta.setSaldoActual(BigDecimal.ZERO); 
                
                // Guardar cuenta
                cuentaRepository.save(nuevaCuenta);
                
            } catch (Exception e) {
                System.err.println("Error creando cuenta automática: " + e.getMessage());
            }
        }

        // D. Enviar correo y redirigir
        emailService.sendWelcomeEmail(email, fullname);
        redirectAttributes.addFlashAttribute("success", "¡Cuenta creada con éxito! Por favor, inicia sesión.");
        
        return "redirect:/login";
    }


    // ==========================================
    // 4. LOGIN INTELIGENTE (Soporta Migración)
    // ==========================================
    @PostMapping("/login")
    public String handleLogin(@RequestParam("email") String email, 
                              @RequestParam("password") String password, // Contraseña escrita por el usuario
                              RedirectAttributes redirectAttributes, 
                              Model model) {
        
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
        
        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();
            String dbPassword = usuario.getPassword(); // Contraseña guardada en BD

            // PASO 1: Verificar si es hash seguro (Usuarios Nuevos / Ya Migrados)
            boolean esSegura = passwordEncoder.matches(password, dbPassword);
            
            // PASO 2: Verificar si es texto plano (Usuarios Antiguos)
            boolean esPlana = password.equals(dbPassword);

            // Si coincide cualquiera de las dos formas
            if (esSegura || esPlana) {
                
                // --- AUTO-MIGRACIÓN DE SEGURIDAD ---
                // Si entró con contraseña plana, la encriptamos AHORA MISMO para el futuro
                if (esPlana) {
                    usuario.setPassword(passwordEncoder.encode(password));
                    usuarioRepository.save(usuario);
                    System.out.println(">>> SEGURIDAD: Usuario " + usuario.getEmail() + " migrado a encriptación exitosamente.");
                }
                // -----------------------------------
                
                // Login exitoso: Guardar en sesión
                model.addAttribute("usuarioLogueado", usuario);
                
                // Redirección por ROL
                String rol = usuario.getRole();

                if ("maestro".equals(rol)) {
                    return "redirect:/dashboardAdmin"; // Acceso total
                } else if ("administrador".equals(rol)) {
                    return "redirect:/reportesAdmin";  // Acceso limitado
                } else {
                    return "redirect:/dashboardUsuarios"; // Cliente
                }
            }
        }
        
        // Login fallido
        redirectAttributes.addFlashAttribute("error", "Correo electrónico o contraseña incorrectos.");
        return "redirect:/login";
    }
}
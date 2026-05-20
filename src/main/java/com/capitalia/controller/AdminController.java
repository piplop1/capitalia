package com.capitalia.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.capitalia.model.Auditoria;
import com.capitalia.model.Configuracion;
import com.capitalia.model.Usuario;
import com.capitalia.repository.AuditoriaRepository;
import com.capitalia.repository.ConfiguracionRepository;
import com.capitalia.repository.CuentaBancariaRepository;
import com.capitalia.repository.PrestamoRepository;
import com.capitalia.repository.SolicitudPrestamoRepository;
import com.capitalia.repository.UsuarioRepository;
import com.capitalia.service.IUsuarioService;
import com.capitalia.service.PrestamoService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private CuentaBancariaRepository cuentaRepository;
    
    @Autowired
    private SolicitudPrestamoRepository solicitudRepository;
    
    @Autowired
    private PrestamoRepository prestamoRepository;
    
    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private ConfiguracionRepository configRepository;

    @Autowired
    private AuditoriaRepository auditoriaRepository;


    // --- MÉTODO AUXILIAR PARA VALIDAR ROL MAESTRO ---
    private boolean esMaestro(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        return u != null && "maestro".equals(u.getRole());
    }


    // =======================================================
    // 1. DASHBOARD (Solo Maestro puede ver administradores)
    // =======================================================
    @GetMapping("/dashboardAdmin")
    public String adminDashboardPage(Model model, HttpSession session) {
        if (!esMaestro(session)) return "redirect:/reportesAdmin"; // Redirige si no es maestro

        model.addAttribute("administradores", usuarioRepository.findAllByRole("administrador"));
        return "admin/dashboard";
    }

    // =======================================================
    // 2. GESTIÓN DE USUARIOS (Solo Maestro puede editar clientes)
    // =======================================================
    @GetMapping("/gesUsuarios")
    public String gestionUsuariosPage(Model model, HttpSession session) {
        if (!esMaestro(session)) return "redirect:/reportesAdmin";

        model.addAttribute("usuarios", usuarioRepository.findAllByRole("usuario"));
        return "admin/usuarios";
    }


    // =======================================================
    // 3. BANDEJA DE SOLICITUDES (Acceso para Admin y Maestro)
    // =======================================================
    @GetMapping("/solicitudesAdmin")
    public String solicitudesPage(Model model) {
        model.addAttribute("solicitudes", solicitudRepository.findByEstado("PENDIENTE"));
        return "admin/solicitudes";
    }

    @PostMapping("/solicitudes/procesar")
    public String procesarSolicitud(@RequestParam("id") Integer id, 
                                    @RequestParam("accion") String accion, 
                                    RedirectAttributes redirectAttributes) {
        try {
            prestamoService.procesarSolicitud(id, accion);
            String mensaje = "APROBAR".equals(accion) ? "Préstamo aprobado y desembolsado." : "Solicitud rechazada.";
            
            auditoriaRepository.save(new Auditoria("Admin/Maestro", "PROCESAR_SOLICITUD", accion + " solicitud #" + id));
            
            redirectAttributes.addFlashAttribute("successMessage", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/solicitudesAdmin";
    }


    // =======================================================
    // 4. REPORTES (Acceso para Admin y Maestro)
    // =======================================================
    @GetMapping("/reportesAdmin")
    public String reportesAdminPage(Model model) {
        long totalUsuarios = usuarioRepository.count();
        BigDecimal totalDinero = cuentaRepository.sumarSaldoTotal();
        if (totalDinero == null) totalDinero = BigDecimal.ZERO;
        long prestamosActivos = prestamoRepository.countByEstadoPrestamo("ACTIVO"); 

        model.addAttribute("totalUsuarios", totalUsuarios);
        model.addAttribute("totalDinero", totalDinero);
        model.addAttribute("prestamosActivos", prestamosActivos);

        return "admin/reportes";
    }

    @GetMapping("/api/admin/stats")
    @ResponseBody
    public Map<String, Object> obtenerEstadisticas() {
        long aprobadas = solicitudRepository.countByEstado("APROBADO");
        long rechazadas = solicitudRepository.countByEstado("RECHAZADO");
        long pendientes = solicitudRepository.countByEstado("PENDIENTE");

        return Map.of("solicitudes", Map.of(
            "Aprobadas", aprobadas, 
            "Rechazadas", rechazadas, 
            "Pendientes", pendientes
        ));
    }


    // =======================================================
    // 5. CONFIGURACIÓN (Acceso para Admin y Maestro)
    // =======================================================
    @GetMapping("/configuracionAdmin")
    public String configuracionPage(Model model) {
        model.addAttribute("configs", configRepository.findAll());
        return "admin/configuracion";
    }

    @PostMapping("/admin/configuracion/guardar")
    public String guardarConfiguracion(@RequestParam("clave") String clave,
                                       @RequestParam("valor") BigDecimal valor,
                                       RedirectAttributes redirectAttributes) {
        try {
            Configuracion config = configRepository.findById(clave).orElse(null);
            if (config != null) {
                String valorAntiguo = config.getValor().toString();
                config.setValor(valor);
                configRepository.save(config);
                
                auditoriaRepository.save(new Auditoria("Admin/Maestro", "EDITAR_CONFIG", "Cambió " + clave + " de " + valorAntiguo + " a " + valor));

                redirectAttributes.addFlashAttribute("successMessage", "Parámetro actualizado correctamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar: " + e.getMessage());
        }
        return "redirect:/configuracionAdmin";
    }


    // =======================================================
    // 6. AUDITORÍA (Solo Maestro)
    // =======================================================
    @GetMapping("/admin/auditoria")
    public String verAuditoria(Model model, HttpSession session) {
        if (!esMaestro(session)) return "redirect:/reportesAdmin";

        model.addAttribute("auditorias", auditoriaRepository.findAll());
        return "admin/auditoria";
    }


    // =======================================================
    // 7. ACCIONES CRUD (Solo Maestro debería poder crear/borrar usuarios)
    // =======================================================
    
    @PostMapping("/crearAdmin")
    public String crearAdmin(@RequestParam("fullname") String fullname, 
                             @RequestParam("email") String email,
                             @RequestParam("password") String password, 
                             RedirectAttributes redirectAttributes, HttpSession session) {
        
        if (!esMaestro(session)) return "redirect:/reportesAdmin"; // Protección extra

        if (usuarioRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("errorAdmin", "El correo electrónico ya está en uso.");
            return "redirect:/dashboardAdmin";
        }
        Usuario nuevoAdmin = new Usuario(fullname, email, password, "administrador");
        usuarioService.guardarUsuario(nuevoAdmin);
        
        auditoriaRepository.save(new Auditoria("Maestro", "CREAR_ADMIN", "Creó nuevo admin: " + email));

        redirectAttributes.addFlashAttribute("successAdmin", "Nuevo administrador creado con éxito.");
        return "redirect:/dashboardAdmin";
    }

    @PostMapping("/usuarios/editar")
    public String editarUsuario(@RequestParam("id") Integer id, 
                                @RequestParam("fullname") String fullname,
                                @RequestParam("email") String email, 
                                @RequestParam("role") String role,
                                RedirectAttributes redirectAttributes, HttpSession session) {
        
        if (!esMaestro(session)) return "redirect:/reportesAdmin";

        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setFullname(fullname);
            usuario.setEmail(email);
            usuario.setRole(role);
            usuarioRepository.save(usuario);
            
            auditoriaRepository.save(new Auditoria("Maestro", "EDITAR_USUARIO", "Editó usuario ID: " + id));

            redirectAttributes.addFlashAttribute("successMessage", "Usuario actualizado con éxito.");
            return "redirect:/gesUsuarios";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Usuario no encontrado.");
            return "redirect:/gesUsuarios";
        });
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!esMaestro(session)) return "redirect:/reportesAdmin";

        usuarioRepository.deleteById(id);
        auditoriaRepository.save(new Auditoria("Maestro", "ELIMINAR_USUARIO", "Eliminó usuario ID: " + id));

        redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado con éxito.");
        return "redirect:/gesUsuarios";
    }

    @PostMapping("/admin/editar")
    public String editarAdmin(@RequestParam("id") Integer id, 
                              @RequestParam("fullname") String fullname,
                              @RequestParam("email") String email, 
                              RedirectAttributes redirectAttributes, HttpSession session) {
        
        if (!esMaestro(session)) return "redirect:/reportesAdmin";

        return usuarioRepository.findById(id).map(admin -> {
            admin.setFullname(fullname);
            admin.setEmail(email);
            usuarioRepository.save(admin);
            
            auditoriaRepository.save(new Auditoria("Maestro", "EDITAR_ADMIN", "Editó admin ID: " + id));
            
            redirectAttributes.addFlashAttribute("successAdmin", "Administrador actualizado con éxito.");
            return "redirect:/dashboardAdmin";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorAdmin", "Error: Administrador no encontrado.");
            return "redirect:/dashboardAdmin";
        });
    }

    @PostMapping("/admin/eliminar/{id}")
    public String eliminarAdmin(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (!esMaestro(session)) return "redirect:/reportesAdmin";

        if (!usuarioRepository.existsById(id)) {
            redirectAttributes.addFlashAttribute("errorAdmin", "Error: El administrador no existe.");
            return "redirect:/dashboardAdmin";
        }
        usuarioRepository.deleteById(id);
        auditoriaRepository.save(new Auditoria("Maestro", "ELIMINAR_ADMIN", "Eliminó admin ID: " + id));
        
        redirectAttributes.addFlashAttribute("successAdmin", "Administrador eliminado con éxito.");
        return "redirect:/dashboardAdmin";
    }
}
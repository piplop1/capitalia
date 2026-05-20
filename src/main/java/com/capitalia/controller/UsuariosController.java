package com.capitalia.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.capitalia.model.Beneficio;
import com.capitalia.model.CronogramaPago; // Importante
import com.capitalia.model.CuentaBancaria;
import com.capitalia.model.Persona;
import com.capitalia.model.Prestamo;
import com.capitalia.model.SolicitudPrestamo;
import com.capitalia.model.Tarjeta;
import com.capitalia.model.Transaccion;
import com.capitalia.model.Usuario;
import com.capitalia.repository.BeneficioRepository;
import com.capitalia.repository.CronogramaPagoRepository; // Importante
import com.capitalia.repository.CuentaBancariaRepository;
import com.capitalia.repository.PrestamoRepository;
import com.capitalia.repository.SolicitudPrestamoRepository;
import com.capitalia.repository.TarjetaRepository;
import com.capitalia.repository.TransaccionRepository;
import com.capitalia.repository.UsuarioRepository;
import com.capitalia.service.PrestamoService;

import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("usuarioLogueado")
public class UsuariosController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CuentaBancariaRepository cuentaRepository;
    
    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private SolicitudPrestamoRepository solicitudRepository;

    @Autowired
    private BeneficioRepository beneficioRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    // --- CORRECCIÓN: AQUÍ FALTABA ESTE REPOSITORIO ---
    @Autowired
    private CronogramaPagoRepository cronogramaRepository;


    // =======================================================
    // 1. GESTIÓN DE PRÉSTAMOS (Solicitar y Pagar)
    // =======================================================
    
    @PostMapping("/prestamos/solicitar")
    public String solicitarPrestamo(
            @RequestParam("monto") BigDecimal monto,
            @RequestParam("plazo") Integer plazo,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        try {
            SolicitudPrestamo solicitud = new SolicitudPrestamo();
            solicitud.setUsuario(usuario);
            solicitud.setMontoSolicitado(monto);
            solicitud.setPlazoMeses(plazo);
            solicitud.setEstado("PENDIENTE");
            solicitud.setFechaSolicitud(LocalDateTime.now());

            solicitudRepository.save(solicitud);

            redirectAttributes.addFlashAttribute("successMessage", "Tu solicitud de préstamo por S/ " + monto + " ha sido enviada a revisión.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar solicitud: " + e.getMessage());
        }

        return "redirect:/serviciosUsuarios";
    }

    @PostMapping("/cuotas/pagar")
    public String pagarCuota(@RequestParam("idCronograma") Integer idCronograma,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        try {
            prestamoService.pagarCuota(idCronograma);
            redirectAttributes.addFlashAttribute("successMessage", "¡Cuota pagada exitosamente!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/serviciosUsuarios";
    }

    @PostMapping("/prestamos/pagar")
    public String pagarPrestamo(@RequestParam("idPrestamo") Integer idPrestamo, RedirectAttributes redirectAttributes) {
        try {
            prestamoService.pagarPrestamo(idPrestamo);
            redirectAttributes.addFlashAttribute("successMessage", "¡Deuda pagada exitosamente! Tu saldo ha sido actualizado.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al pagar: " + e.getMessage());
        }
        return "redirect:/serviciosUsuarios";
    }


    // =======================================================
    // 2. VISTA DE SERVICIOS (Historial + Solicitudes + Deudas + Cronograma)
    // =======================================================
    @GetMapping("/serviciosUsuarios")
    public String serviciosUsuariosPage(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        // A. Cargar Historial y Deudas
        Optional<CuentaBancaria> cuentaOpt = cuentaRepository.findByUsuario_Id(usuario.getId());

        if (cuentaOpt.isPresent()) {
            // 1. Historial de movimientos
            List<Transaccion> movimientos = transaccionRepository.findByCuentaBancaria_IdCuenta(cuentaOpt.get().getIdCuenta());
            model.addAttribute("movimientos", movimientos);
            
            // 2. Préstamos Activos
            List<Prestamo> misPrestamos = prestamoRepository.findByCuentaDesembolso_Usuario_Id(usuario.getId());
            model.addAttribute("misPrestamos", misPrestamos);

            // Calcular total deuda por préstamo
            Map<Integer, BigDecimal> totalesDeuda = new HashMap<>();
            for (Prestamo p : misPrestamos) {
                BigDecimal total = p.getMontoAprobado()
                        .multiply(BigDecimal.ONE.add(p.getTasaInteresAplicada()))
                        .setScale(2, RoundingMode.HALF_UP);
                totalesDeuda.put(p.getIdPrestamo(), total);
            }
            model.addAttribute("totalesDeuda", totalesDeuda);

            // 3. Cronograma Completo (Aquí se usaba cronogramaRepository sin declarar)
            List<CronogramaPago> cronogramaTotal = cronogramaRepository.findByPrestamo_CuentaDesembolso_Usuario_IdOrderByPrestamo_FechaDesembolsoDescFechaVencimientoAsc(usuario.getId());
            model.addAttribute("cronogramaTotal", cronogramaTotal);

        } else {
            model.addAttribute("movimientos", Collections.emptyList());
            model.addAttribute("misPrestamos", Collections.emptyList());
            model.addAttribute("cronogramaTotal", Collections.emptyList());
        }

        // B. Solicitudes
        List<SolicitudPrestamo> misSolicitudes = solicitudRepository.findByUsuario_Id(usuario.getId());
        model.addAttribute("misSolicitudes", misSolicitudes);

        return "usuario/servicios";
    }


    // =======================================================
    // 3. GESTIÓN DE PERFIL
    // =======================================================
    @GetMapping("/miPerfilUsuarios")
    public String miPerfilUsuariosPage(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        usuario = usuarioRepository.findById(usuario.getId()).orElse(usuario);
        Persona persona = usuario.getPersona();
        if (persona == null) persona = new Persona();

        model.addAttribute("usuarioLogueado", usuario);
        model.addAttribute("persona", persona);
        return "usuario/perfil";
    }

    @PostMapping("/miPerfilUsuarios/actualizar")
    public String actualizarPerfil(
            @RequestParam("fullname") String fullname,
            @RequestParam(value = "dni", required = false) String dni,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "direccion", required = false) String direccion,
            @RequestParam(value = "fechaNacimiento", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaNacimiento,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) return "redirect:/login";

        Usuario usuarioBD = usuarioRepository.findById(usuarioSesion.getId()).orElse(null);

        if (usuarioBD != null) {
            usuarioBD.setFullname(fullname);
            Persona persona = usuarioBD.getPersona();
            if (persona == null) {
                persona = new Persona();
                usuarioBD.setPersona(persona);
            }
            persona.setDni(dni);
            persona.setTelefono(telefono);
            persona.setDireccion(direccion);
            persona.setFechaNacimiento(fechaNacimiento);

            usuarioRepository.save(usuarioBD);
            session.setAttribute("usuarioLogueado", usuarioBD);

            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado con éxito.");
        }

        return "redirect:/miPerfilUsuarios";
    }


    // =======================================================
    // 4. VISTA DE BENEFICIOS
    // =======================================================
    @GetMapping("/misBeneficiosUsuarios")
    public String misBeneficiosUsuariosPage(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";
        
        Optional<CuentaBancaria> cuentaOpt = cuentaRepository.findByUsuario_Id(usuario.getId());
        BigDecimal saldo = cuentaOpt.isPresent() ? cuentaOpt.get().getSaldoActual() : BigDecimal.ZERO;
        model.addAttribute("saldo", saldo);

        int nivelId = 1; 
        if (saldo.doubleValue() >= 3500) nivelId = 4;
        else if (saldo.doubleValue() >= 2100) nivelId = 3;
        else if (saldo.doubleValue() >= 1200) nivelId = 2;

        List<Beneficio> misBeneficios = beneficioRepository.findBeneficiosPorNivel(nivelId);
        model.addAttribute("misBeneficios", misBeneficios);
        model.addAttribute("nivelIdActual", nivelId);

        return "usuario/beneficios";
    }


    // =======================================================
    // 5. CONFIGURACIONES
    // =======================================================
    @GetMapping("/configuracionesUsuarios")
    public String configuracionesUsuariosPage() {
        return "usuario/configuracion";
    }

    // Acción: Agregar Tarjeta
    @PostMapping("/config/agregarTarjeta")
    public String agregarTarjeta(@RequestParam("numeroTarjeta") String numero,
                                 @RequestParam("fechaVencimiento") String vencimiento,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        Optional<CuentaBancaria> cuentaOpt = cuentaRepository.findByUsuario_Id(usuario.getId());
        if (cuentaOpt.isPresent()) {
            Tarjeta tarjeta = new Tarjeta();
            tarjeta.setNumeroTarjeta(numero);
            tarjeta.setFechaVencimiento(vencimiento);
            tarjeta.setCuenta(cuentaOpt.get());
            
            tarjetaRepository.save(tarjeta);
            redirectAttributes.addFlashAttribute("successMessage", "Tarjeta agregada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: No tienes una cuenta activa.");
        }
        return "redirect:/configuracionesUsuarios";
    }

    // Acción: Actualizar Celular
    @PostMapping("/config/actualizarCelular")
    public String actualizarCelular(@RequestParam("celular") String celular,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioSesion == null) return "redirect:/login";

        Usuario usuarioBD = usuarioRepository.findById(usuarioSesion.getId()).orElse(null);
        if (usuarioBD != null) {
            Persona persona = usuarioBD.getPersona();
            if (persona == null) {
                persona = new Persona();
                usuarioBD.setPersona(persona);
            }
            persona.setTelefono(celular);
            usuarioRepository.save(usuarioBD);
            redirectAttributes.addFlashAttribute("successMessage", "Número de celular actualizado.");
        }
        return "redirect:/configuracionesUsuarios";
    }

    // Acción: Actualizar Límites
    @PostMapping("/config/actualizarLimites")
    public String actualizarLimites(@RequestParam("limiteDiario") BigDecimal limite,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) return "redirect:/login";

        Optional<CuentaBancaria> cuentaOpt = cuentaRepository.findByUsuario_Id(usuario.getId());
        if (cuentaOpt.isPresent()) {
            CuentaBancaria cuenta = cuentaOpt.get();
            cuenta.setLimiteDiario(limite);
            cuentaRepository.save(cuenta);
            
            redirectAttributes.addFlashAttribute("successMessage", "Límite diario actualizado correctamente.");
        }
        return "redirect:/configuracionesUsuarios";
    }
}
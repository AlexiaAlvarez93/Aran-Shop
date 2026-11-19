package org.example.practicaweb.Controller;

import org.example.practicaweb.model.Usuario;
import org.example.practicaweb.repository.UsuarioRepository;
import org.example.practicaweb.repository.ProductoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class InicioControlador {

    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public InicioControlador(
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // RUTA DE INICIO
    @GetMapping("/")
    public String mostrarInicio(Model model) {

        model.addAttribute("titulo", "Mi primera aplicación con Spring Boot");
        model.addAttribute("mensaje", "La página recibe información desde el controlador");
        model.addAttribute("curso", "Programación Avanzada");

        return "index";
    }

    // MOSTRAR LOGIN
    @GetMapping("/login")
    public String mostrarLogin(HttpSession session) {

        String rol = (String) session.getAttribute("rol");

        if ("ADMIN".equals(rol)) {
            return "redirect:/menu";
        }

        if ("CLIENTE".equals(rol)) {
            return "redirect:/tienda";
        }

        return "login";
    }

    // PROCESAR LOGIN
    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam("usuario") String usuario,
            @RequestParam("contrasena") String contrasena,
            Model model,
            HttpSession session) {

        Optional<Usuario> usuarioEncontrado =
                usuarioRepository.findByUsuario(usuario.trim());

        if (usuarioEncontrado.isPresent()) {

            Usuario usuarioBD = usuarioEncontrado.get();

            if (passwordEncoder.matches(
                    contrasena,
                    usuarioBD.getContrasena())) {

                if ("ADMIN".equals(usuarioBD.getRol())) {

                    session.setAttribute("usuarioId", usuarioBD.getId());
                    session.setAttribute("nombreUsuario", usuarioBD.getNombre());
                    session.setAttribute("rol", usuarioBD.getRol());

                    return "redirect:/menu";

                } else if ("CLIENTE".equals(usuarioBD.getRol())) {

                    session.setAttribute("usuarioId", usuarioBD.getId());
                    session.setAttribute("nombreUsuario", usuarioBD.getNombre());
                    session.setAttribute("rol", usuarioBD.getRol());

                    return "redirect:/tienda";
                }
            }
        }

        model.addAttribute("error", "Usuario o contraseña incorrectos");
        return "login";
    }

    // MENU PRINCIPAL DEL ADMINISTRADOR
    @GetMapping("/menu")
    public String mostrarMenu(HttpSession session) {

        String rol = (String) session.getAttribute("rol");

        if (!"ADMIN".equals(rol)) {
            return "redirect:/login";
        }

        return "menu";
    }

    // RUTA DE MENSAJES DEL ADMINISTRADOR
    @GetMapping("/mensajes")
    public String mostrarMensajes(
            Model model,
            HttpSession session) {

        String rol = (String) session.getAttribute("rol");

        if (!"ADMIN".equals(rol)) {
            return "redirect:/login";
        }

        model.addAttribute("mensaje", "Sección de mensajes del sistema");

        return "mensajes";
    }

    // MOSTRAR TIENDA DEL CLIENTE
    @GetMapping("/tienda")
    public String mostrarTienda(
            Model model,
            HttpSession session) {

        String rol = (String) session.getAttribute("rol");

        if (!"CLIENTE".equals(rol)) {
            return "redirect:/login";
        }

        model.addAttribute("productos", productoRepository.findAll());

        return "tienda";
    }

    // CERRAR SESIÓN
    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }
}
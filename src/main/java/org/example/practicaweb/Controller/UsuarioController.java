package org.example.practicaweb.Controller;

import org.example.practicaweb.model.Usuario;
import org.example.practicaweb.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Configuration
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner crearAdministrador() {
        return args -> {
            if (!usuarioRepository.existsByUsuario("admin")) {
                Usuario admin = new Usuario(
                        "Administrador",
                        "admin",
                        passwordEncoder.encode("1234"),
                        "ADMIN"
                );

                usuarioRepository.save(admin);
            }
        };
    }

    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrarUsuario(
            @RequestParam("nombre") String nombre,
            @RequestParam("usuario") String usuario,
            @RequestParam("contrasena") String contrasena,
            Model model) {

        if (nombre.trim().isEmpty()
                || usuario.trim().isEmpty()
                || contrasena.isEmpty()) {

            model.addAttribute("error", "Todos los campos son obligatorios.");
            return "registro";
        }

        if (usuarioRepository.existsByUsuario(usuario.trim())) {
            model.addAttribute("error", "Ese nombre de usuario ya está registrado.");
            return "registro";
        }

        if (contrasena.length() < 6) {
            model.addAttribute("error", "La contraseña debe tener al menos 6 caracteres.");
            return "registro";
        }

        Usuario nuevoUsuario = new Usuario(
                nombre.trim(),
                usuario.trim(),
                passwordEncoder.encode(contrasena),
                "CLIENTE"
        );

        usuarioRepository.save(nuevoUsuario);

        model.addAttribute("mensaje", "¡Tu cuenta fue creada correctamente! Ya puedes iniciar sesión.");

        return "registro";
    }
}
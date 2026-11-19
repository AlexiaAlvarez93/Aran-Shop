package org.example.practicaweb.Controller;

import org.example.practicaweb.model.Pedido;
import org.example.practicaweb.repository.PedidoRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MisPedidosController {

    private final PedidoRepository pedidoRepository;

    public MisPedidosController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/mis-pedidos")
    public String mostrarMisPedidos(HttpSession session, Model model) {

        String rol = (String) session.getAttribute("rol");
        String nombreCliente = (String) session.getAttribute("nombreUsuario");

        if (!"CLIENTE".equals(rol) || nombreCliente == null) {
            return "redirect:/login";
        }

        List<Pedido> pedidos = pedidoRepository.findAll()
                .stream()
                .filter(p -> p.getNombreCliente() != null
                        && p.getNombreCliente().equals(nombreCliente))
                .collect(Collectors.toList());

        model.addAttribute("pedidos", pedidos);

        return "mis-pedidos";
    }
}

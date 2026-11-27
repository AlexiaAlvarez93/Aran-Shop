package org.example.practicaweb.Controller;

import org.example.practicaweb.model.Pedido;
import org.example.practicaweb.repository.PedidoRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class MisPedidosController {

    private final PedidoRepository pedidoRepository;

    public MisPedidosController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/mis-pedidos")
    public String mostrarMisPedidos(HttpSession session, Model model) {

        String rol = (String) session.getAttribute("rol");
        Long usuarioId = (Long) session.getAttribute("usuarioId");

        if (!"CLIENTE".equals(rol) || usuarioId == null) {
            return "redirect:/login";
        }

        List<Pedido> pedidos =
                pedidoRepository.findByUsuario_Id(usuarioId);

        model.addAttribute("pedidos", pedidos);

        return "mis-pedidos";
    }
}
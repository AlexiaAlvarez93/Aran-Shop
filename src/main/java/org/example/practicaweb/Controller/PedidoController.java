package org.example.practicaweb.Controller;

import org.example.practicaweb.model.Pedido;
import org.example.practicaweb.model.Producto;
import org.example.practicaweb.model.DetallePedido;

import org.example.practicaweb.repository.PedidoRepository;
import org.example.practicaweb.repository.ProductoRepository;
import org.example.practicaweb.repository.DetallePedidoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class PedidoController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    // VERIFICAR QUE EL USUARIO SEA ADMINISTRADOR
    private boolean esAdministrador(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("rol"));
    }

    // MOSTRAR LISTA DE PEDIDOS
    @GetMapping("/pedidos")
    public String listarPedidos(Model model, HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        return cargarPedidos(null, null, model);
    }

    // BUSCAR Y FILTRAR PEDIDOS
    @GetMapping("/pedidos/buscar")
    public String buscarPedidos(
            @RequestParam(required = false) String nombreCliente,
            @RequestParam(required = false) String estado,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        return cargarPedidos(nombreCliente, estado, model);
    }

    // CARGAR PEDIDOS CON O SIN FILTROS
    private String cargarPedidos(String nombreCliente, String estado, Model model) {

        List<Pedido> pedidos = pedidoRepository.findAll();

        if (nombreCliente != null && !nombreCliente.trim().isEmpty()) {
            pedidos = pedidos.stream()
                    .filter(p -> p.getNombreCliente() != null
                            && p.getNombreCliente().toLowerCase()
                            .contains(nombreCliente.trim().toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (estado != null && !estado.trim().isEmpty()) {
            pedidos = pedidos.stream()
                    .filter(p -> p.getEstado() != null
                            && p.getEstado().equalsIgnoreCase(estado))
                    .collect(Collectors.toList());
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("filtroNombre", nombreCliente);
        model.addAttribute("filtroEstado", estado);

        return "pedidos";
    }

    // MOSTRAR DETALLES DE UN PEDIDO
    @GetMapping("/pedidos/detalles/{id}")
    public String verDetalles(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        Pedido pedido = pedidoRepository.findById(id).orElse(null);

        if (pedido == null) {
            return "redirect:/pedidos";
        }

        List<DetallePedido> detalles =
                detallePedidoRepository.findByPedido_Id(id);

        for (DetallePedido detalle : detalles) {

            BigDecimal subtotal = detalle.getPrecio()
                    .multiply(new BigDecimal(detalle.getCantidad()));

            detalle.setSubtotal(subtotal);
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("detalles", detalles);

        return "pedido-detalles";
    }

    // REGISTRAR PEDIDO DESDE EL PANEL DEL ADMINISTRADOR
    @Transactional
    @PostMapping("/pedidos/guardar")
    public String guardarPedido(
            @RequestParam String nombreCliente,
            @RequestParam("productoIds") List<Long> productoIds,
            @RequestParam("cantidades") List<Integer> cantidades,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        if (nombreCliente == null
                || nombreCliente.trim().isEmpty()
                || productoIds == null
                || cantidades == null
                || productoIds.isEmpty()
                || productoIds.size() != cantidades.size()) {

            redirectAttributes.addFlashAttribute(
                    "error", "Verifique los datos del pedido.");

            return "redirect:/pedidos";
        }

        Map<Long, Integer> cantidadesPorProducto = new HashMap<>();

        for (int i = 0; i < productoIds.size(); i++) {

            if (productoIds.get(i) == null
                    || cantidades.get(i) == null
                    || cantidades.get(i) <= 0) {

                redirectAttributes.addFlashAttribute(
                        "error", "Seleccione productos y cantidades válidas.");

                return "redirect:/pedidos";
            }

            cantidadesPorProducto.merge(
                    productoIds.get(i),
                    cantidades.get(i),
                    Integer::sum);
        }

        Map<Long, Producto> productosSeleccionados = new HashMap<>();

        for (Map.Entry<Long, Integer> entrada : cantidadesPorProducto.entrySet()) {

            Producto producto = productoRepository
                    .findById(entrada.getKey()).orElse(null);

            if (producto == null) {

                redirectAttributes.addFlashAttribute(
                        "error", "Uno de los productos seleccionados no existe.");

                return "redirect:/pedidos";
            }

            if (producto.getCantidad() == null
                    || producto.getCantidad() < entrada.getValue()) {

                redirectAttributes.addFlashAttribute(
                        "error", "No hay suficiente inventario para el producto: "
                                + producto.getNombre());

                return "redirect:/pedidos";
            }

            productosSeleccionados.put(producto.getId(), producto);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (int i = 0; i < productoIds.size(); i++) {

            Producto producto = productosSeleccionados.get(productoIds.get(i));

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(cantidades.get(i)));

            total = total.add(subtotal);
        }

        Pedido pedido = new Pedido();

        pedido.setNombreCliente(nombreCliente.trim());
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado("Pendiente");
        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        for (int i = 0; i < productoIds.size(); i++) {

            Producto producto = productosSeleccionados.get(productoIds.get(i));

            DetallePedido detalle = new DetallePedido();

            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidades.get(i));
            detalle.setPrecio(producto.getPrecio());

            detallePedidoRepository.save(detalle);

            producto.setCantidad(
                    producto.getCantidad() - cantidades.get(i));
        }

        productoRepository.saveAll(productosSeleccionados.values());

        redirectAttributes.addFlashAttribute(
                "exito", "Pedido registrado correctamente.");

        return "redirect:/pedidos";
    }

    // CONFIRMAR COMPRA DESDE LA TIENDA DEL CLIENTE
    @Transactional
    @PostMapping("/tienda/comprar")
    public String comprarDesdeTienda(
            @RequestParam(value = "productoIds", required = false)
            List<Long> productoIds,

            @RequestParam(value = "cantidades", required = false)
            List<Integer> cantidades,

            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // VERIFICAR QUE EL CLIENTE HAYA INICIADO SESIÓN
        String rol = (String) session.getAttribute("rol");
        String nombreCliente = (String) session.getAttribute("nombreUsuario");

        if (!"CLIENTE".equals(rol) || nombreCliente == null) {

            redirectAttributes.addFlashAttribute(
                    "error", "Debe iniciar sesión como cliente para comprar.");

            return "redirect:/login";
        }

        // VALIDAR LOS PRODUCTOS RECIBIDOS
        if (productoIds == null
                || cantidades == null
                || productoIds.isEmpty()
                || productoIds.size() != cantidades.size()) {

            redirectAttributes.addFlashAttribute(
                    "error", "El carrito está vacío o contiene datos incorrectos.");

            return "redirect:/tienda";
        }

        // AGRUPAR CANTIDADES POR PRODUCTO
        Map<Long, Integer> cantidadesPorProducto = new HashMap<>();

        for (int i = 0; i < productoIds.size(); i++) {

            if (productoIds.get(i) == null
                    || cantidades.get(i) == null
                    || cantidades.get(i) <= 0) {

                redirectAttributes.addFlashAttribute(
                        "error", "Verifique las cantidades de los productos.");

                return "redirect:/tienda";
            }

            cantidadesPorProducto.merge(
                    productoIds.get(i),
                    cantidades.get(i),
                    Integer::sum);
        }

        // CONSULTAR Y VALIDAR INVENTARIO
        Map<Long, Producto> productosSeleccionados = new HashMap<>();

        for (Map.Entry<Long, Integer> entrada : cantidadesPorProducto.entrySet()) {

            Producto producto = productoRepository
                    .findById(entrada.getKey()).orElse(null);

            if (producto == null) {

                redirectAttributes.addFlashAttribute(
                        "error", "Uno de los productos ya no está disponible.");

                return "redirect:/tienda";
            }

            if (producto.getCantidad() == null
                    || producto.getCantidad() < entrada.getValue()) {

                redirectAttributes.addFlashAttribute(
                        "error", "No hay suficiente inventario para: "
                                + producto.getNombre());

                return "redirect:/tienda";
            }

            productosSeleccionados.put(producto.getId(), producto);
        }

        // CALCULAR EL TOTAL
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entrada : cantidadesPorProducto.entrySet()) {

            Producto producto = productosSeleccionados.get(entrada.getKey());

            BigDecimal subtotal = producto.getPrecio()
                    .multiply(BigDecimal.valueOf(entrada.getValue()));

            total = total.add(subtotal);
        }

        // CREAR EL PEDIDO CON EL NOMBRE DE LA SESIÓN
        Pedido pedido = new Pedido();

        pedido.setNombreCliente(nombreCliente);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado("Pendiente");
        pedido.setTotal(total);

        pedidoRepository.save(pedido);

        // GUARDAR DETALLES Y DESCONTAR INVENTARIO
        for (Map.Entry<Long, Integer> entrada : cantidadesPorProducto.entrySet()) {

            Producto producto = productosSeleccionados.get(entrada.getKey());

            Integer cantidad = entrada.getValue();

            DetallePedido detalle = new DetallePedido();

            detalle.setPedido(pedido);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecio(producto.getPrecio());

            detallePedidoRepository.save(detalle);

            producto.setCantidad(producto.getCantidad() - cantidad);
        }

        productoRepository.saveAll(productosSeleccionados.values());

        redirectAttributes.addFlashAttribute(
                "exito", "¡Tu compra fue realizada correctamente!");

        return "redirect:/tienda";
    }

    // ACTUALIZAR ESTADO DEL PEDIDO
    @PostMapping("/pedidos/actualizarEstado")
    public String actualizarEstado(
            @RequestParam Long id,
            @RequestParam String estado,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        Pedido pedido = pedidoRepository.findById(id).orElse(null);

        if (pedido == null) {

            redirectAttributes.addFlashAttribute(
                    "error", "No se encontró el pedido.");

            return "redirect:/pedidos";
        }

        if (!estado.equals("Pendiente")
                && !estado.equals("En preparación")
                && !estado.equals("Entregado")) {

            redirectAttributes.addFlashAttribute(
                    "error", "El estado seleccionado no es válido.");

            return "redirect:/pedidos";
        }

        pedido.setEstado(estado);

        pedidoRepository.save(pedido);

        redirectAttributes.addFlashAttribute(
                "exito", "Estado del pedido actualizado correctamente.");

        return "redirect:/pedidos";
    }
}
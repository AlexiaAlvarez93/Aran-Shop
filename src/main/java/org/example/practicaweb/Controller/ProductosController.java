package org.example.practicaweb.Controller;

import org.example.practicaweb.model.Producto;
import org.example.practicaweb.model.Categoria;
import org.example.practicaweb.repository.ProductoRepository;
import org.example.practicaweb.repository.CategoriaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class ProductosController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    // VERIFICAR QUE EL USUARIO SEA ADMINISTRADOR
    private boolean esAdministrador(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("rol"));
    }

    // MOSTRAR LISTA DE PRODUCTOS
    @GetMapping("/productos")
    public String listarProductos(Model model, HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("producto", new Producto());
        model.addAttribute("mensaje", "Panel de gestión de productos - Arán Shop");

        return "productos";
    }

    // BUSCAR PRODUCTOS POR NOMBRE
    @GetMapping("/productos/buscar")
    public String buscarProductos(
            @RequestParam String nombre,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute("productos",
                productoRepository.findByNombreContainingIgnoreCase(nombre));

        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("producto", new Producto());
        model.addAttribute("mensaje", "Resultados de búsqueda - Arán Shop");
        model.addAttribute("busqueda", nombre);

        return "productos";
    }

    // GUARDAR PRODUCTO
    @PostMapping("/productos/guardar")
    public String guardarProducto(
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) Long categoriaId,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        if (nombre.trim().isEmpty()
                || precio.compareTo(BigDecimal.ZERO) < 0
                || cantidad < 0
                || categoriaId == null) {

            redirectAttributes.addFlashAttribute("error",
                    "Verifique los datos del producto.");
            return "redirect:/productos";
        }

        Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);

        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Debe seleccionar una categoría válida.");
            return "redirect:/productos";
        }

        Producto producto = new Producto();
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCantidad(cantidad);
        producto.setCategoria(categoria);

        productoRepository.save(producto);

        redirectAttributes.addFlashAttribute("exito",
                "Producto registrado correctamente.");

        return "redirect:/productos";
    }

    // MOSTRAR PRODUCTO PARA EDITAR
    @GetMapping("/productos/editar/{id}")
    public String editarProducto(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) {
            return "redirect:/productos";
        }

        model.addAttribute("productos", productoRepository.findAll());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("producto", producto);
        model.addAttribute("mensaje", "Editar producto - Arán Shop");

        return "productos";
    }

    // ACTUALIZAR PRODUCTO
    @PostMapping("/productos/actualizar")
    public String actualizarProducto(
            @RequestParam Long id,
            @RequestParam String nombre,
            @RequestParam BigDecimal precio,
            @RequestParam Integer cantidad,
            @RequestParam(required = false) Long categoriaId,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) {
            redirectAttributes.addFlashAttribute("error",
                    "No se encontró el producto.");
            return "redirect:/productos";
        }

        if (nombre.trim().isEmpty()
                || precio.compareTo(BigDecimal.ZERO) < 0
                || cantidad < 0
                || categoriaId == null) {

            redirectAttributes.addFlashAttribute("error",
                    "Verifique los datos del producto.");
            return "redirect:/productos";
        }

        Categoria categoria = categoriaRepository.findById(categoriaId).orElse(null);

        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Debe seleccionar una categoría válida.");
            return "redirect:/productos";
        }

        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCantidad(cantidad);
        producto.setCategoria(categoria);

        productoRepository.save(producto);

        redirectAttributes.addFlashAttribute("exito",
                "Producto actualizado correctamente.");

        return "redirect:/productos";
    }

    // ELIMINAR PRODUCTO
    @PostMapping("/productos/eliminar/{id}")
    public String eliminarProducto(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        Producto producto = productoRepository.findById(id).orElse(null);

        if (producto == null) {
            redirectAttributes.addFlashAttribute("error",
                    "No se encontró el producto que desea eliminar.");
            return "redirect:/productos";
        }

        try {
            productoRepository.delete(producto);

            redirectAttributes.addFlashAttribute("exito",
                    "Producto eliminado correctamente.");

        } catch (DataIntegrityViolationException e) {

            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar este producto porque está asociado a pedidos.");
        }

        return "redirect:/productos";
    }
}
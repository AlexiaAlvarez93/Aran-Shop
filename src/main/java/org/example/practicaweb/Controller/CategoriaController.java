package org.example.practicaweb.Controller;

import org.example.practicaweb.model.Categoria;
import org.example.practicaweb.repository.CategoriaRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // VERIFICAR QUE EL USUARIO SEA ADMINISTRADOR
    private boolean esAdministrador(HttpSession session) {
        return "ADMIN".equals(session.getAttribute("rol"));
    }

    // MOSTRAR CATEGORIAS
    @GetMapping("/categorias")
    public String listarCategorias(Model model, HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("categoria", new Categoria());

        return "categorias";
    }

    // GUARDAR CATEGORIA
    @PostMapping("/categorias/guardar")
    public String guardarCategoria(
            @RequestParam String nombre,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Debe escribir el nombre de la categoría.");
            return "redirect:/categorias";
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(nombre.trim());

        categoriaRepository.save(categoria);

        redirectAttributes.addFlashAttribute("exito",
                "Categoría registrada correctamente.");

        return "redirect:/categorias";
    }

    // MOSTRAR CATEGORIA PARA EDITAR
    @GetMapping("/categorias/editar/{id}")
    public String editarCategoria(
            @PathVariable Long id,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        Categoria categoria = categoriaRepository.findById(id).orElse(null);

        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error",
                    "La categoría no existe.");
            return "redirect:/categorias";
        }

        model.addAttribute("categoriaEditar", categoria);
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("categoria", new Categoria());

        return "categorias";
    }

    // ACTUALIZAR CATEGORIA
    @PostMapping("/categorias/actualizar")
    public String actualizarCategoria(
            @RequestParam Long id,
            @RequestParam String nombre,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "Debe escribir el nombre de la categoría.");
            return "redirect:/categorias";
        }

        Categoria categoria = categoriaRepository.findById(id).orElse(null);

        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error",
                    "La categoría no existe.");
            return "redirect:/categorias";
        }

        categoria.setNombre(nombre.trim());
        categoriaRepository.save(categoria);

        redirectAttributes.addFlashAttribute("exito",
                "Categoría actualizada correctamente.");

        return "redirect:/categorias";
    }

    // ELIMINAR CATEGORIA
    @PostMapping("/categorias/eliminar/{id}")
    public String eliminarCategoria(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        Categoria categoria = categoriaRepository.findById(id).orElse(null);

        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error",
                    "La categoría no existe.");
            return "redirect:/categorias";
        }

        try {
            categoriaRepository.delete(categoria);

            redirectAttributes.addFlashAttribute("exito",
                    "Categoría eliminada correctamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "No se puede eliminar esta categoría porque tiene productos asociados.");
        }

        return "redirect:/categorias";
    }
}
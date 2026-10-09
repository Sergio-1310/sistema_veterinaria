package com.example.huellitasVet.controller;

import com.example.huellitasVet.model.Veterinario;
import com.example.huellitasVet.service.VeterinarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor 
@RequestMapping("/veterinarios")
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    // 1. Cargar la vista principal (con layout o solo fragment si es HTMX desde el sidebar)
    @GetMapping
    public String vistaVeterinarios(@RequestHeader(value = "HX-Request", required = false) boolean isHtmx, Model model) {
        model.addAttribute("veterinarios", veterinarioService.listarTodos());
        if (isHtmx) {
            return "veterinarios/index :: content";
        }
        return "veterinarios/index";
    }

    // 2. Filtrado en tiempo real de la tabla (buscador)
    @GetMapping("/tabla")
    public String tablaFiltrada(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        model.addAttribute("veterinarios", veterinarioService.buscar(buscar));
        return "veterinarios/fragments/veterinarios-filas :: filas";
    }

    // 3. Modal para Crear
    @GetMapping("/modal-formulario")
    public String modalNuevo(Model model) {
        model.addAttribute("veterinario", new Veterinario());
        return "veterinarios/fragments/modal-veterinario :: formulario";
    }

    // 4. Modal para Editar
    @GetMapping("/modal-formulario/{id}")
    public String modalEditar(@PathVariable Long id, Model model) {
        model.addAttribute("veterinario", veterinarioService.buscarPorId(id));
        return "veterinarios/fragments/modal-veterinario :: formulario";
    }

    // 5. Guardar (creación o edición) y refrescar la tabla
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Veterinario veterinario, Model model, HttpServletResponse response) {
        veterinarioService.guardar(veterinario);
        
        // Cabecera HTMX para que el modal se cierre automáticamente
        response.setHeader("HX-Trigger", "cerrarModalVeterinario");
        
        model.addAttribute("veterinarios", veterinarioService.listarTodos());
        return "veterinarios/fragments/veterinarios-filas :: filas";
    }
}
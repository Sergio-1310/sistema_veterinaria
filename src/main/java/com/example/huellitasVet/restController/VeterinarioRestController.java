package com.example.huellitasVet.restController;

import com.example.huellitasVet.dto.Solicitudes.*;
import com.example.huellitasVet.model.Veterinario;
import com.example.huellitasVet.service.VeterinarioService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/veterinarios")
@Transactional
public class VeterinarioRestController {
    private final VeterinarioService veterinarios;

    @GetMapping
    public List<Veterinario> listar(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Boolean activo) {
        return veterinarios.buscar(q).stream()
                .filter(v -> activo == null || activo.equals(v.getActivo()))
                .toList();
    }

    @GetMapping("/{id}")
    public Veterinario detalle(@PathVariable Long id) {
        return veterinarios.buscarPorId(id);
    }

    private Veterinario guardar(Veterinario v, Medico datos) {
        v.setNombres(datos.nombres().trim());
        v.setApellidos(datos.apellidos().trim());
        v.setCorreo(datos.correo().trim());
        v.setTelefono(datos.telefono());
        v.setEspecialidad(datos.especialidad());
        return veterinarios.guardar(v);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Veterinario crear(@Valid @RequestBody Medico datos) {
        return guardar(new Veterinario(), datos);
    }

    @PutMapping("/{id}")
    public Veterinario editar(@PathVariable Long id, @Valid @RequestBody Medico datos) {
        return guardar(detalle(id), datos);
    }

    @PatchMapping("/{id}/activo")
    public Veterinario activar(@PathVariable Long id, @Valid @RequestBody Activacion datos) {
        var v = detalle(id);
        v.setActivo(datos.activo());
        return veterinarios.guardar(v);
    }
}

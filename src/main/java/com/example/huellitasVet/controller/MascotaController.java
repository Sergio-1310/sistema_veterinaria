package com.example.huellitasVet.controller;

import com.example.huellitasVet.dto.Solicitudes.Paciente;
import com.example.huellitasVet.model.Mascota;
import com.example.huellitasVet.service.MascotaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {
    private final MascotaService mascotas;

    @GetMapping
    public List<Mascota> listar(
            @RequestParam(required = false) Long propietarioId,
            @RequestParam(defaultValue = "") String especie,
            @RequestParam(defaultValue = "") String q) {
        return mascotas.buscar(propietarioId, especie, q);
    }

    @GetMapping("/{id}")
    public Mascota detalle(@PathVariable Long id) {
        return mascotas.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mascota crear(@Valid @RequestBody Paciente datos) {
        return mascotas.guardar(null, datos);
    }

    @PutMapping("/{id}")
    public Mascota editar(@PathVariable Long id, @Valid @RequestBody Paciente datos) {
        return mascotas.guardar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        mascotas.eliminar(id);
    }
}

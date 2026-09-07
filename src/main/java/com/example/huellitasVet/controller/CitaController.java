package com.example.huellitasVet.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.huellitasVet.model.Cita;
import com.example.huellitasVet.service.CitaService;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public List<Cita> listar() {
        return citaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cita> buscar(
            @PathVariable Long id) {

        Cita cita = citaService.buscarPorId(id);

        if (cita == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(cita);
    }

    @PostMapping
    public ResponseEntity<Cita> registrar(
            @RequestBody Cita cita) {

        Cita nueva = citaService.registrar(cita);

        return ResponseEntity.status(201).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> actualizar(
            @PathVariable Long id,
            @RequestBody Cita cita) {

        Cita actualizada = citaService.actualizar(id, cita);

        if (actualizada == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        boolean eliminado = citaService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
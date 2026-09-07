package com.example.huellitasVet.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.huellitasVet.model.Mascota;
import com.example.huellitasVet.service.MascotaService;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public List<Mascota> listar() {
        return mascotaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mascota> buscarPorId(
            @PathVariable Long id) {

        Mascota mascota = mascotaService.buscarPorId(id);

        if (mascota == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mascota);
    }

    @PostMapping
    public ResponseEntity<Mascota> registrar(
            @RequestBody Mascota mascota) {

        Mascota nueva = mascotaService.registrar(mascota);

        return ResponseEntity.status(201).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizar(
            @PathVariable Long id,
            @RequestBody Mascota mascota) {

        Mascota actualizada = mascotaService.actualizar(id, mascota);

        if (actualizada == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        boolean eliminado = mascotaService.eliminar(id);

        if (!eliminado) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
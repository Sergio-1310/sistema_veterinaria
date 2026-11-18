package com.example.huellitasVet.controller;

import com.example.huellitasVet.dto.*;
import com.example.huellitasVet.service.CitaService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/citas")
public class CitaController {
    private final CitaService citas;

    @GetMapping
    public List<ResumenCita> listar(
            @RequestParam(required = false) Long mascotaId,
            @RequestParam(required = false) Long veterinarioId,
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "") String q) {
        return citas.buscar(mascotaId, veterinarioId, fecha, estado, q);
    }

    @GetMapping("/{id}")
    public ResumenCita detalle(@PathVariable Long id) {
        return citas.detalle(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumenCita crear(@Valid @RequestBody Solicitudes.Turno datos) {
        return citas.guardar(null, datos);
    }

    @PutMapping("/{id}")
    public ResumenCita editar(@PathVariable Long id, @Valid @RequestBody Solicitudes.Turno datos) {
        return citas.guardar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        citas.cancelar(id);
    }

    @PostMapping("/{id}/atencion")
    @ResponseStatus(HttpStatus.CREATED)
    public FichaClinica atender(
            @PathVariable Long id,
            @Valid @RequestBody Solicitudes.Consulta datos,
            Authentication auth) {
        return citas.atender(
                id,
                datos,
                auth.getName(),
                auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}

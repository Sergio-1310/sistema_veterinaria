package com.example.huellitasVet.restController;

import com.example.huellitasVet.dto.*;
import com.example.huellitasVet.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Transactional(readOnly = true)
public class ClinicaController {
    private final AtencionRepository atenciones;
    private final CitaRepository citas;
    private final MascotaRepository mascotas;

    @GetMapping("/historial")
    public List<FichaClinica> historial(
            @RequestParam(required = false) Long mascotaId,
            @RequestParam(defaultValue = "") String q) {
        return atenciones.buscar(mascotaId, q).stream().map(FichaClinica::de).toList();
    }

    @GetMapping("/historial/{id}")
    public FichaClinica ficha(@PathVariable Long id) {
        return FichaClinica.de(
                atenciones.findById(id).orElseThrow(() -> ErroresApi.ausente("Registro clínico")));
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        var ahora = LocalDateTime.now();
        return Map.of(
                "mascotas",
                mascotas.count(),
                "hoy",
                citas.countByEstadoAndFechaProgramada("PROGRAMADA", ahora.toLocalDate()),
                "pendientes",
                citas.countByEstado("PROGRAMADA"),
                "proximas",
                citas
                        .proximas(ahora.toLocalDate(), ahora.toLocalTime(), PageRequest.of(0, 6))
                        .stream()
                        .map(ResumenCita::de)
                        .toList());
    }
}

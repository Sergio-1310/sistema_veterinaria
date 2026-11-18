package com.example.huellitasVet.service;

import static com.example.huellitasVet.restController.ErroresApi.*;

import com.example.huellitasVet.dto.*;
import com.example.huellitasVet.model.*;
import com.example.huellitasVet.repository.*;

import jakarta.persistence.*;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CitaServiceImpl implements CitaService {
    private final CitaRepository citas;
    private final MascotaRepository mascotas;
    private final AtencionRepository atenciones;
    private final EntityManager entidades;

    public List<ResumenCita> buscar(
            Long mascotaId, Long veterinarioId, LocalDate fecha, String estado, String q) {
        return citas.buscar(mascotaId, veterinarioId, fecha, estado, q).stream()
                .map(ResumenCita::de)
                .toList();
    }

    public ResumenCita detalle(Long id) {
        return ResumenCita.de(citas.findById(id).orElseThrow(() -> ausente("Turno")));
    }

    private Cita programada(Long id) {
        var cita = citas.paraEditar(id).orElseThrow(() -> ausente("Turno"));
        if (!"PROGRAMADA".equals(cita.getEstado()))
            throw conflicto("La cita debe estar programada.");
        return cita;
    }

    public ResumenCita guardar(Long id, Solicitudes.Turno datos) {
        Cita cita = id == null ? new Cita() : programada(id);
        if (LocalDateTime.of(datos.fecha(), datos.hora()).isBefore(LocalDateTime.now())
                || datos.hora().getSecond() != 0
                || datos.hora().getNano() != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Elige una fecha futura y una hora con precisión de minutos.");
        }
        var mascota = mascotas.findById(datos.mascotaId()).orElseThrow(() -> ausente("Paciente"));
        var vet =
                entidades.find(
                        Veterinario.class, datos.veterinarioId(), LockModeType.PESSIMISTIC_WRITE);
        if (vet == null) throw ausente("Veterinario");
        if (!Boolean.TRUE.equals(vet.getActivo())) throw conflicto("El veterinario está inactivo.");
        if (citas.existsByVeterinarioIdAndFechaProgramadaAndHoraProgramadaAndEstadoAndIdNot(
                vet.getId(), datos.fecha(), datos.hora(), "PROGRAMADA", id == null ? -1L : id))
            throw conflicto("El veterinario ya tiene una cita a esa hora.");
        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        cita.setFechaProgramada(datos.fecha());
        cita.setHoraProgramada(datos.hora());
        cita.setMotivo(datos.motivo().trim());
        cita.setEstado("PROGRAMADA");
        return ResumenCita.de(citas.save(cita));
    }

    public void cancelar(Long id) {
        programada(id).setEstado("CANCELADA");
    }

    public FichaClinica atender(
            Long id, Solicitudes.Consulta datos, String email, boolean administrador) {
        var cita = programada(id);
        if (cita.getMascota() == null || cita.getVeterinario() == null)
            throw conflicto("Asocia una mascota y un veterinario antes de atender.");
        if (!administrador && !cita.getVeterinario().getCorreo().equalsIgnoreCase(email))
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "La cita está asignada a otro veterinario.");
        var atencion = new Atencion();
        atencion.setCita(cita);
        atencion.setFecha(LocalDate.now());
        atencion.setDiagnostico(datos.diagnostico().trim());
        atencion.setTratamiento(datos.tratamiento().trim());
        atencion.setObservaciones(datos.observaciones());
        cita.setEstado("ATENDIDA");
        return FichaClinica.de(atenciones.saveAndFlush(atencion));
    }
}

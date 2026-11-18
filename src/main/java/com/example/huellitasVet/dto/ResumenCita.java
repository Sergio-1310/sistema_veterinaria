package com.example.huellitasVet.dto;

import com.example.huellitasVet.model.Cita;

public record ResumenCita(
        Long id,
        Long mascotaId,
        String mascotaNombre,
        Long veterinarioId,
        String veterinarioNombre,
        String fecha,
        String hora,
        String motivo,
        String estado) {
    public static ResumenCita de(Cita cita) {
        var mascota = cita.getMascota();
        var vet = cita.getVeterinario();
        return new ResumenCita(
                cita.getId(),
                mascota == null ? cita.getMascotaIdAnterior() : mascota.getId(),
                mascota == null ? null : mascota.getNombre(),
                vet == null ? null : vet.getId(),
                vet == null ? null : vet.getNombres() + " " + vet.getApellidos(),
                cita.getFechaProgramada() == null
                        ? cita.getFechaAnterior()
                        : cita.getFechaProgramada().toString(),
                cita.getHoraProgramada() == null
                        ? cita.getHoraAnterior()
                        : cita.getHoraProgramada().toString(),
                cita.getMotivo(),
                cita.getEstado());
    }
}

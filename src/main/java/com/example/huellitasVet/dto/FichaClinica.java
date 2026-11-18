package com.example.huellitasVet.dto;

import com.example.huellitasVet.model.Atencion;

import java.time.LocalDate;

public record FichaClinica(
        Long id,
        LocalDate fecha,
        ResumenCita cita,
        String diagnostico,
        String tratamiento,
        String observaciones) {
    public static FichaClinica de(Atencion a) {
        return new FichaClinica(
                a.getId(),
                a.getFecha(),
                ResumenCita.de(a.getCita()),
                a.getDiagnostico(),
                a.getTratamiento(),
                a.getObservaciones());
    }
}

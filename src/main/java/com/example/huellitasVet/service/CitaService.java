package com.example.huellitasVet.service;

import com.example.huellitasVet.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface CitaService {
    List<ResumenCita> buscar(
            Long mascotaId, Long veterinarioId, LocalDate fecha, String estado, String q);

    ResumenCita detalle(Long id);

    ResumenCita guardar(Long id, Solicitudes.Turno datos);

    void cancelar(Long id);

    FichaClinica atender(Long id, Solicitudes.Consulta datos, String email, boolean administrador);
}

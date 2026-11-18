package com.example.huellitasVet.service;

import com.example.huellitasVet.dto.Solicitudes.Paciente;
import com.example.huellitasVet.model.Mascota;

import java.util.List;

public interface MascotaService {
    List<Mascota> buscar(Long propietarioId, String especie, String q);

    Mascota buscarPorId(Long id);

    Mascota guardar(Long id, Paciente datos);

    void eliminar(Long id);
}

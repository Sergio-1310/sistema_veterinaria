package com.example.huellitasVet.service;

import java.util.List;

import com.example.huellitasVet.model.Cita;

public interface CitaService {
    List<Cita> listar();

    Cita buscarPorId(Long id);

    Cita registrar(Cita cita);

    Cita actualizar(Long id, Cita cita);

    boolean eliminar(Long id);
}

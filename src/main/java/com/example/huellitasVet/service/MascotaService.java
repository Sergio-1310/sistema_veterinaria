package com.example.huellitasVet.service;

import java.util.List;

import com.example.huellitasVet.model.Mascota;

public interface MascotaService {

    List<Mascota> listar();

    Mascota buscarPorId(Long id);

    Mascota registrar(Mascota mascota);

    Mascota actualizar(Long id, Mascota mascota);

    boolean eliminar(Long id);
}

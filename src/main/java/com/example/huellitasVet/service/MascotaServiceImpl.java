package com.example.huellitasVet.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.huellitasVet.model.Mascota;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final List<Mascota> mascotas = new ArrayList<>();
    private Long siguienteId = 1L;

    public MascotaServiceImpl() {

        mascotas.add(new Mascota(
                siguienteId++,
                "Max",
                "Perro",
                "Labrador",
                4,
                "Carlos Pérez"));

        mascotas.add(new Mascota(
                siguienteId++,
                "Luna",
                "Gato",
                "Siamés",
                2,
                "Ana Torres"));
    }

    @Override
    public List<Mascota> listar() {
        return mascotas;
    }

    @Override
    public Mascota buscarPorId(Long id) {
        return mascotas.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Mascota registrar(Mascota mascota) {
        mascota.setId(siguienteId++);
        mascotas.add(mascota);
        return mascota;
    }

    @Override
    public Mascota actualizar(Long id, Mascota mascota) {

        Mascota existente = buscarPorId(id);

        if (existente == null) {
            return null;
        }

        existente.setNombre(mascota.getNombre());
        existente.setEspecie(mascota.getEspecie());
        existente.setRaza(mascota.getRaza());
        existente.setEdad(mascota.getEdad());
        existente.setNombrePropietario(mascota.getNombrePropietario());

        return existente;
    }

    @Override
    public boolean eliminar(Long id) {
        return mascotas.removeIf(
                mascota -> mascota.getId().equals(id));
    }
}

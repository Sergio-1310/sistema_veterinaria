package com.example.huellitasVet.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.huellitasVet.model.Cita;

@Service
public class CitaServiceImpl implements CitaService {

    private final List<Cita> citas = new ArrayList<>();
    private Long siguienteId = 1L;

    public CitaServiceImpl() {

        citas.add(new Cita(
                siguienteId++,
                1L,
                "2026-09-10",
                "10:00",
                "Consulta general",
                "PENDIENTE"));
    }

    @Override
    public List<Cita> listar() {
        return citas;
    }

    @Override
    public Cita buscarPorId(Long id) {
        return citas.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Cita registrar(Cita cita) {
        cita.setId(siguienteId++);

        if (cita.getEstado() == null) {
            cita.setEstado("PENDIENTE");
        }

        citas.add(cita);

        return cita;
    }

    @Override
    public Cita actualizar(Long id, Cita cita) {

        Cita existente = buscarPorId(id);

        if (existente == null) {
            return null;
        }

        existente.setMascotaId(cita.getMascotaId());
        existente.setFecha(cita.getFecha());
        existente.setHora(cita.getHora());
        existente.setMotivo(cita.getMotivo());
        existente.setEstado(cita.getEstado());

        return existente;
    }

    @Override
    public boolean eliminar(Long id) {
        return citas.removeIf(
                cita -> cita.getId().equals(id));
    }
}

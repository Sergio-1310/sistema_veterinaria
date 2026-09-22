package com.example.huellitasVet.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.huellitasVet.model.Cita;
import com.example.huellitasVet.repository.CitaRepository;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;

    public CitaServiceImpl(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public List<Cita> listar() {
        return citaRepository.findAll();
    }

    @Override
    public Cita buscarPorId(Long id) {
        return citaRepository.findById(id).orElse(null);
    }

    @Override
    public Cita registrar(Cita cita) {

        if (cita.getEstado() == null) {
            cita.setEstado("PENDIENTE");
        }

        return citaRepository.save(cita);
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

        return citaRepository.save(existente);
    }

    @Override
    public boolean eliminar(Long id) {

        if (!citaRepository.existsById(id)) {
            return false;
        }

        citaRepository.deleteById(id);
        return true;
    }
}
